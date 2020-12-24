package org.tbk.spring.testcontainer.electrumd.config;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.testcontainer.core.CustomHostPortWaitStrategy;
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;
import org.tbk.spring.testcontainer.electrumx.config.ElectrumxContainerAutoConfiguration;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static org.tbk.spring.testcontainer.core.MoreTestcontainers.buildInternalContainerUrlWithoutProtocol;

@Slf4j
@Configuration
@EnableConfigurationProperties(ElectrumDaemonContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.testcontainer.electrum-daemon.enabled", havingValue = "true")
@AutoConfigureAfter(ElectrumxContainerAutoConfiguration.class)
public class ElectrumDaemonContainerAutoConfiguration {

    // currently only the image from "osminogin" is supported
    private static final String DOCKER_IMAGE_NAME = "osminogin/electrum-daemon:3.3.8";

    private static final DockerImageName dockerImageName = DockerImageName.parse(DOCKER_IMAGE_NAME);

    private static final int DEFAULT_RPC_PORT = 7000;

    private static final List<Integer> hardcodedStandardPorts = ImmutableList.<Integer>builder()
            .add(DEFAULT_RPC_PORT)
            .build();

    private final ElectrumDaemonContainerProperties properties;

    public ElectrumDaemonContainerAutoConfiguration(ElectrumDaemonContainerProperties properties) {
        this.properties = requireNonNull(properties);
    }


    @Bean(name = "electrumDaemonContainer", destroyMethod = "stop")
    @ConditionalOnMissingBean(ElectrumxContainer.class)
    public ElectrumDaemonContainer<?> electrumDaemonContainer(@Qualifier("electrumDaemonContainerWaitStrategy") WaitStrategy waitStrategy) {
        ElectrumDaemonContainer<?> electrumDaemonContainer = createStartedElectrumDaemonContainer(waitStrategy);

        if (log.isDebugEnabled()) {
            log.debug("Started {} with exposed ports {}", electrumDaemonContainer.getContainerName(), electrumDaemonContainer.getExposedPorts());
        }

        return electrumDaemonContainer;
    }

    @Bean(name = "electrumDaemonContainer", destroyMethod = "stop")
    @ConditionalOnBean(ElectrumxContainer.class)
    public ElectrumDaemonContainer<?> electrumDaemonContainerWithElectrumxTestcontainer(@Qualifier("electrumDaemonContainerWaitStrategy") WaitStrategy waitStrategy,
                                                                                        ElectrumxContainer<?> electrumxContainer) {

        verifyCompatibilityWithElectrumx(electrumxContainer);

        String serverUrl = String.format("%s:s", buildInternalContainerUrlWithoutProtocol(electrumxContainer, 50002));

        ElectrumDaemonContainer<?> electrumDaemonContainer = createStartedElectrumDaemonContainer(waitStrategy, () -> Optional.of(serverUrl));

        if (log.isDebugEnabled()) {
            log.debug("Started {} with exposed ports {}", electrumDaemonContainer.getContainerName(), electrumDaemonContainer.getExposedPorts());
        }

        return electrumDaemonContainer;
    }

    @Bean("electrumDaemonContainerWaitStrategy")
    @ConditionalOnMissingBean(name = "electrumDaemonContainerWaitStrategy")
    public WaitStrategy electrumDaemonContainerWaitStrategy() {
        return CustomHostPortWaitStrategy.builder()
                .ports(hardcodedStandardPorts)
                .build();
    }

    private void verifyCompatibilityWithElectrumx(ElectrumxContainer<?> electrumxContainer) {
        Map<String, String> env = this.properties.getEnvironmentWithDefaults();
        String electrumxContainerNetwork = electrumxContainer.getEnvMap().getOrDefault("NET", "regtest");
        String electrumDaemonNetwork = env.getOrDefault("ELECTRUM_NETWORK", "regtest");

        boolean networksOfClientAndServerAreCompatible = electrumDaemonNetwork.equals(electrumxContainerNetwork);
        if (!networksOfClientAndServerAreCompatible) {
            String errorMessage = String.format("Electrum Daemon and ElectrumX run on different networks! daemon: %s, server: %s", electrumDaemonNetwork, electrumxContainerNetwork);
            throw new IllegalStateException(errorMessage);
        }
    }

    private ElectrumDaemonContainer<?> createStartedElectrumDaemonContainer(WaitStrategy waitStrategy) {
        return createStartedElectrumDaemonContainer(waitStrategy, Optional::empty);
    }

    private ElectrumDaemonContainer<?> createStartedElectrumDaemonContainer(WaitStrategy waitStrategy,
                                                                            Supplier<Optional<String>> serverUrlSupplier) {
        Map<String, String> env = this.properties.getEnvironmentWithDefaults();

        ElectrumDaemonContainer<?> electrumDaemonContainer = new ElectrumDaemonContainer<>(dockerImageName)
                .withCreateContainerCmdModifier(cmdModifier())
                .withExposedPorts(hardcodedStandardPorts.toArray(new Integer[]{}))
                .withEnv(env)
                .waitingFor(waitStrategy);

        copyWalletToContainerIfNecessary(electrumDaemonContainer);

        electrumDaemonContainer.start();

        restartDaemonWithCustomizedSettings(electrumDaemonContainer, serverUrlSupplier);

        return electrumDaemonContainer;
    }

    private Consumer<CreateContainerCmd> cmdModifier() {
        return MoreTestcontainers.cmdModifiers().withName(dockerContainerName());
    }

    private String dockerContainerName() {
        return String.format("%s-%s", dockerImageName.getUnversionedPart(),
                Integer.toHexString(System.identityHashCode(this)))
                .replace("/", "-");
    }

    private void copyWalletToContainerIfNecessary(ElectrumDaemonContainer<?> electrumDaemonContainer) {
        Optional<MountableFile> mountableWalletOrEmpty = this.properties.getDefaultWallet()
                .map(MountableFile::forClasspathResource);

        if (mountableWalletOrEmpty.isPresent()) {
            String home = this.properties.getElectrumHomeDir();

            // There are different wallet directories per network:
            // - mainnet: /home/electrum/.electrum/wallets,
            // - testnet: /home/electrum/.electrum/testnet/wallets
            // - regtest: /home/electrum/.electrum/regtest/wallets
            // - regtest: /home/electrum/.electrum/simnet/wallets
            String networkWalletDir = home + "/.electrum" + Optional.of(this.properties.getNetwork())
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

    private void restartDaemonWithCustomizedSettings(ElectrumDaemonContainer<?> electrumDaemonContainer, Supplier<Optional<String>> serverUrlSupplier) {

        daemonStop(electrumDaemonContainer);

        setupDefaultConfigValuesHack(electrumDaemonContainer);

        serverUrlSupplier.get().ifPresent(serverUrl -> {
            setupConnectingToServerHack(electrumDaemonContainer, serverUrl);
        });

        daemonStart(electrumDaemonContainer);

        loadWalletIfNecessary(electrumDaemonContainer, Duration.ofMillis(3_000));
    }

    /**
     * Currently a ugly hack: osminogins docker image is currently not able
     * to specify the arguments during startup. A workaround is to start the container as it is
     * and then setting all necessary config options. When the daemon restarts it will pick up
     * the proper settings.
     * TODO: try to make the docker setup more customizable e.g. like lnzap/docker-lnd does.
     */
    private void setupConnectingToServerHack(ElectrumDaemonContainer<?> electrumDaemonContainer, String serverUrl) {
        updateElectrumConfigHack(electrumDaemonContainer, "server", serverUrl);
        updateElectrumConfigHack(electrumDaemonContainer, "oneserver", "true");
    }

    private void setupDefaultConfigValuesHack(ElectrumDaemonContainer<?> electrumDaemonContainer) {
        updateElectrumConfigHack(electrumDaemonContainer, "auto_connect", "true");
        updateElectrumConfigHack(electrumDaemonContainer, "log_to_file", "true");
        updateElectrumConfigHack(electrumDaemonContainer, "check_updates", "false");
        updateElectrumConfigHack(electrumDaemonContainer, "dont_show_testnet_warning", "true");
    }

    private Optional<String> networkFlag() {
        return Optional.of(this.properties.getNetwork())
                .filter(it -> !"mainnet".equals(it))
                .map(it -> "--" + it);
    }

    private void daemonStart(ElectrumDaemonContainer<?> electrumDaemonContainer) {
        daemonExec(electrumDaemonContainer, "start");
    }

    private void daemonStop(ElectrumDaemonContainer<?> electrumDaemonContainer) {
        daemonExec(electrumDaemonContainer, "stop");
    }

    private void loadWalletIfNecessary(ElectrumDaemonContainer<?> electrumDaemonContainer, Duration sleepDuration) {
        if (this.properties.getDefaultWallet().isPresent()) {
            try {
                Thread.sleep(sleepDuration.toMillis()); // let the daemon some time to startup; 3000ms seems to be enough

                daemonExec(electrumDaemonContainer, "load_wallet");
            } catch (InterruptedException e) {
                throw new RuntimeException("Error while adapting electrum-daemon: restart with auto-loading wallet failed", e);
            }
        }
    }

    private void daemonExec(ElectrumDaemonContainer<?> electrumDaemonContainer, String command) {
        try {
            Optional<String> networkFlag = networkFlag();

            if (networkFlag.isEmpty()) {
                electrumDaemonContainer.execInContainer("electrum", "daemon", command);
            } else {
                electrumDaemonContainer.execInContainer("electrum", networkFlag.get(), "daemon", command);
            }
        } catch (InterruptedException | IOException e) {
            String errorMessage = String.format("Error while executing `electrum daemon %s`", command);
            throw new RuntimeException(errorMessage, e);
        }
    }

    private void updateElectrumConfigHack(ElectrumDaemonContainer<?> electrumDaemonContainer, String key, String value) {
        try {
            Optional<String> networkFlag = networkFlag();

            if (networkFlag.isEmpty()) {
                electrumDaemonContainer.execInContainer("electrum", "setconfig", key, value);
            } else {
                electrumDaemonContainer.execInContainer("electrum", networkFlag.get(), "setconfig", key, value);
            }
        } catch (InterruptedException | IOException e) {
            String errorMessage = String.format("Error while executing `electrum setconfig %s %s`", key, value);
            throw new RuntimeException(errorMessage, e);
        }
    }
}
