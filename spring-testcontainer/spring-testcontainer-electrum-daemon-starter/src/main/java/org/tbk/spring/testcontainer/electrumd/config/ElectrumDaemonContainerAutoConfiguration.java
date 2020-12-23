package org.tbk.spring.testcontainer.electrumd.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.testcontainer.core.CustomHostPortWaitStrategy;
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.tbk.spring.testcontainer.core.MoreTestcontainers.buildInternalContainerUrlWithoutProtocol;

@Slf4j
@Configuration
@EnableConfigurationProperties(ElectrumDaemonContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.testcontainer.electrum-daemon.enabled", havingValue = "true")
public class ElectrumDaemonContainerAutoConfiguration {

    // currently only the image from "osminogin" is supported
    private static final String DOCKER_IMAGE_NAME = "osminogin/electrum-daemon:3.3.8";

    private static final DockerImageName dockerImageName = DockerImageName.parse(DOCKER_IMAGE_NAME);

    private final ElectrumDaemonContainerProperties properties;

    public ElectrumDaemonContainerAutoConfiguration(ElectrumDaemonContainerProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean(name = "electrumDaemonContainer", destroyMethod = "stop")
    public ElectrumDaemonContainer<?> electrumDaemonContainer(ElectrumxContainer<?> electrumxContainer) {

        List<Integer> hardcodedStandardPorts = ImmutableList.<Integer>builder()
                .add(this.properties.getRpcPort())
                .build();

        List<Integer> exposedPorts = ImmutableList.<Integer>builder()
                .addAll(hardcodedStandardPorts)
                .build();

        CustomHostPortWaitStrategy waitStrategy = CustomHostPortWaitStrategy.builder()
                .ports(hardcodedStandardPorts)
                .build();

        String dockerContainerName = String.format("%s-%s", dockerImageName.getUnversionedPart(),
                Integer.toHexString(System.identityHashCode(this)))
                .replace("/", "-");

        String network = electrumxContainer.getEnvMap().getOrDefault("NET", "regtest");

        ImmutableMap<String, String> env = ImmutableMap.<String, String>builder()
                .putAll(buildEnvMap())
                .put("ELECTRUM_NETWORK", network)
                .build();

        String home = Optional.ofNullable(env.get("ELECTRUM_HOME"))
                .orElseThrow(() -> new IllegalStateException("Cannot find env ELECTRUM_HOME"));

        ElectrumDaemonContainer<?> electrumDaemonContainer = new ElectrumDaemonContainer<>(dockerImageName)
                .withWorkingDirectory(home)
                .withCreateContainerCmdModifier(MoreTestcontainers.cmdModifiers().withName(dockerContainerName))
                .withExposedPorts(exposedPorts.toArray(new Integer[]{}))
                .withEnv(env)
                .waitingFor(waitStrategy);

        copyWalletToContainerIfNecessary(electrumDaemonContainer, env);

        electrumDaemonContainer.start();

        restartDaemonWithCustomizedSettings(electrumxContainer, electrumDaemonContainer, env);

        return electrumDaemonContainer;
    }

    private void copyWalletToContainerIfNecessary(ElectrumDaemonContainer<?> electrumDaemonContainer, Map<String, String> env) {
        Optional<MountableFile> mountableWalletOrEmpty = this.properties.getDefaultWallet()
                .map(MountableFile::forClasspathResource);

        if (mountableWalletOrEmpty.isPresent()) {
            String home = Optional.ofNullable(env.get("ELECTRUM_HOME"))
                    .orElseThrow(() -> new IllegalStateException("Cannot find env ELECTRUM_HOME"));

            String electrumNetwork = Optional.ofNullable(env.get("ELECTRUM_NETWORK"))
                    .orElseThrow(() -> new IllegalStateException("Cannot find env ELECTRUM_NETWORK"));

            // There are different wallet directories per network:
            // - mainnet: /home/electrum/.electrum/wallets,
            // - testnet: /home/electrum/.electrum/testnet/wallets
            // - regtest: /home/electrum/.electrum/regtest/wallets
            // - regtest: /home/electrum/.electrum/simnet/wallets
            String networkWalletDir = home + "/.electrum" + Optional.of(electrumNetwork)
                    .filter(it -> !"mainnet".equals(it))
                    .map(it -> "/" + it + "/wallets")
                    .orElse("/wallets");

            String containerWalletFilePath = networkWalletDir + "/default_wallet";

            MountableFile mountableWallet = mountableWalletOrEmpty.get();
            if (log.isDebugEnabled()) {
                String filesystemPath = mountableWallet.getFilesystemPath();
                log.debug("copy file to container: {} -> {}", filesystemPath, containerWalletFilePath);
            }

            electrumDaemonContainer.withCopyFileToContainer(mountableWallet, containerWalletFilePath);
        }
    }

    private void restartDaemonWithCustomizedSettings(ElectrumxContainer<?> electrumxContainer, ElectrumDaemonContainer<?> electrumDaemonContainer, ImmutableMap<String, String> env) {
        try {
            String electrumNetwork = Optional.ofNullable(env.get("ELECTRUM_NETWORK"))
                    .orElseThrow(() -> new IllegalStateException("Cannot find env ELECTRUM_NETWORK"));

            String networkFlag = Optional.of(electrumNetwork)
                    .filter(it -> !"mainnet".equals(it))
                    .map(it -> "--" + it)
                    .orElse("");

            electrumDaemonContainer.execInContainer("electrum", networkFlag, "daemon", "stop");

            setupConnectingToLocalElectrumxServerHack(electrumxContainer, networkFlag, electrumDaemonContainer);

            electrumDaemonContainer.execInContainer("electrum", networkFlag, "daemon", "start");

            if (this.properties.getDefaultWallet().isPresent()) {
                Thread.sleep(3000); // let the daemon some time to startup; 3000ms seems to be enough

                electrumDaemonContainer.execInContainer("electrum", networkFlag, "daemon", "load_wallet");
            }

        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("Error while adapting electrum-daemon: restart with auto-loaded wallets failed", e);
        }
    }

    private Map<String, String> buildEnvMap() {
        return ImmutableMap.<String, String>builder()
                .put("ELECTRUM_USER", "electrum")
                .put("ELECTRUM_HOME", "/home/electrum")
                .put("ELECTRUM_PASSWORD", "test")
                .build();
    }

    /**
     * Currently a ugly hack: osminogins docker image is currently not able
     * to specify the arguments during startup. A workaround is to start the container as it is
     * and then setting all necessary config options. When the daemon restarts it will pick up
     * the proper settings.
     * TODO: try to make the docker setup more customizable e.g. like lnzap/docker-lnd does.
     */
    private void setupConnectingToLocalElectrumxServerHack(ElectrumxContainer<?> electrumxContainer, String networkFlag, ElectrumDaemonContainer<?> electrumDaemonContainer) {
        try {
            String serverUrl = String.format("%s:s", buildInternalContainerUrlWithoutProtocol(electrumxContainer, 50002));
            electrumDaemonContainer.execInContainer("electrum", networkFlag, "setconfig", "server", serverUrl);

            electrumDaemonContainer.execInContainer("electrum", networkFlag, "setconfig", "oneserver", "true");
            electrumDaemonContainer.execInContainer("electrum", networkFlag, "setconfig", "auto_connect", "true");
            electrumDaemonContainer.execInContainer("electrum", networkFlag, "setconfig", "log_to_file", "true");
            electrumDaemonContainer.execInContainer("electrum", networkFlag, "setconfig", "check_updates", "false");
            electrumDaemonContainer.execInContainer("electrum", networkFlag, "setconfig", "dont_show_testnet_warning", "true");

        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("Error while adapting electrum-daemon: setup local server failed", e);
        }
    }

}
