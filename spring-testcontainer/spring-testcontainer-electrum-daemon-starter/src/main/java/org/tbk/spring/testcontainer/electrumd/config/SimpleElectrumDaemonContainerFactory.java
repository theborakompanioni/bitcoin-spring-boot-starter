package org.tbk.spring.testcontainer.electrumd.config;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.tbk.spring.testcontainer.core.CustomHostPortWaitStrategy;
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.tbk.spring.testcontainer.core.MoreTestcontainers.buildInternalContainerUrlWithoutProtocol;
import static org.tbk.spring.testcontainer.electrumd.config.ElectrumDaemonContainerProperties.ELECTRUM_NETWORK_ENV_NAME;

@Slf4j
public final class SimpleElectrumDaemonContainerFactory {

    @Value
    @Builder
    public static class ElectrumDaemonContainerConfig {
        static final String ELECTRUM_NETWORK_ENV_NAME = "ELECTRUM_NETWORK";

        private static final Map<String, String> defaultEnvironment = ImmutableMap.<String, String>builder()
                .put("ELECTRUM_RPCUSER", "electrum")
                .put("ELECTRUM_RPCPASSWORD", "test")
                .put(ELECTRUM_NETWORK_ENV_NAME, "regtest")
                .build();

        @Singular("addEnvVar")
        Map<String, String> environment;

        String defaultWallet;

        public Optional<String> getDefaultWallet() {
            return Optional.ofNullable(defaultWallet);
        }

        public Map<String, String> getEnvironment() {
            ImmutableMap.Builder<String, String> environmentBuilder = ImmutableMap.<String, String>builder()
                    .putAll(environment);

            defaultEnvironment.forEach((key, value) -> {
                if (!environment.containsKey(key)) {
                    environmentBuilder.put(key, value);
                }
            });

            return environmentBuilder.build();
        }

        public String getNetwork() {
            return environment.getOrDefault(ELECTRUM_NETWORK_ENV_NAME, defaultEnvironment.get(ELECTRUM_NETWORK_ENV_NAME));
        }
    }

    // currently only the image from "theborakompanioni" is supported
    private static final String DOCKER_IMAGE_NAME = "ghcr.io/theborakompanioni/electrum-daemon:4.5.8";

    private static final DockerImageName dockerImageName = DockerImageName.parse(DOCKER_IMAGE_NAME);

    private static final int DEFAULT_RPC_PORT = 7000;

    private static final List<Integer> hardcodedStandardPorts = ImmutableList.<Integer>builder()
            .add(DEFAULT_RPC_PORT)
            .build();

    private static final WaitStrategy containerWaitStrategy = CustomHostPortWaitStrategy.builder()
            .ports(hardcodedStandardPorts)
            .build();

    private static final AtomicLong containerNameIdCounter = new AtomicLong(0L);

    public ElectrumDaemonContainer<?> createStartedElectrumDaemonContainer(ElectrumDaemonContainerConfig config,
                                                                           Container<?> electrumServerContainer) {

        String serverUrl = String.format("%s:s", buildInternalContainerUrlWithoutProtocol(electrumServerContainer, 50002));

        return createStartedElectrumDaemonContainer(config, () -> Optional.of(serverUrl));
    }

    public ElectrumDaemonContainer<?> createStartedElectrumDaemonContainer(ElectrumDaemonContainerConfig config) {
        return createStartedElectrumDaemonContainer(config, Optional::empty);
    }

    public ElectrumDaemonContainer<?> createStartedElectrumDaemonContainer(ElectrumDaemonContainerConfig config,
                                                                           Supplier<Optional<String>> serverUrlSupplier) {
        Map<String, String> env = config.getEnvironment();

        ElectrumDaemonContainer<?> electrumDaemonContainer = new ElectrumDaemonContainer<>(dockerImageName)
                .withCreateContainerCmdModifier(cmdModifier())
                .withExposedPorts(hardcodedStandardPorts.toArray(new Integer[]{}))
                .withEnv(env)
                .waitingFor(containerWaitStrategy);

        copyWalletToContainerIfNecessary(config, electrumDaemonContainer);

        electrumDaemonContainer.start();

        restartDaemonWithCustomizedSettings(config, electrumDaemonContainer, serverUrlSupplier);

        return electrumDaemonContainer;
    }

    private Consumer<CreateContainerCmd> cmdModifier() {
        return MoreTestcontainers.cmdModifiers().withName(dockerContainerName());
    }

    private String dockerContainerName() {
        return String.format("%s-%s-%d", dockerImageName.getUnversionedPart(),
                        Integer.toHexString(System.identityHashCode(this)),
                        containerNameIdCounter.getAndIncrement())
                .replace("/", "-");
    }

    private void copyWalletToContainerIfNecessary(ElectrumDaemonContainerConfig config, ElectrumDaemonContainer<?> electrumDaemonContainer) {
        Optional<MountableFile> mountableWalletOrEmpty = config.getDefaultWallet()
                .map(MountableFile::forClasspathResource);

        if (mountableWalletOrEmpty.isPresent()) {
            String home = "/home/electrum";

            // There are different wallet directories per network:
            // - mainnet: /home/electrum/.electrum/wallets,
            // - testnet: /home/electrum/.electrum/testnet/wallets
            // - regtest: /home/electrum/.electrum/regtest/wallets
            // - simnet: /home/electrum/.electrum/simnet/wallets
            String networkWalletDir = home + "/.electrum" + Optional.of(config.getNetwork())
                    .filter(it -> !"mainnet".equals(it))
                    .map("/%s/wallets"::formatted)
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

    private void restartDaemonWithCustomizedSettings(ElectrumDaemonContainerConfig config, ElectrumDaemonContainer<?> electrumDaemonContainer, Supplier<Optional<String>> serverUrlSupplier) {
        daemonStop(electrumDaemonContainer);

        setupDefaultConfigValuesHack(electrumDaemonContainer);

        serverUrlSupplier.get().ifPresent(serverUrl -> {
            setupConnectingToServerHack(electrumDaemonContainer, serverUrl);
        });

        daemonStart(electrumDaemonContainer);

        // let the daemon some time to startup; 5000ms seems to be enough
        loadWalletIfNecessary(config, electrumDaemonContainer, Duration.ofMillis(5_000));
    }

    /**
     * Currently a ugly hack: osminogins docker image is currently not able
     * to specify the arguments during startup. A workaround is to start the container as it is
     * and then apply all necessary config options. When the daemon restarts it will pick up
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

    private Optional<String> networkFlag(ElectrumDaemonContainer<?> container) {
        return Optional.of(container.getEnvMap())
                .map(it -> it.get(ELECTRUM_NETWORK_ENV_NAME))
                .filter(it -> !"mainnet".equals(it))
                .map(it -> "--" + it);
    }

    private void daemonStart(ElectrumDaemonContainer<?> electrumDaemonContainer) {
        daemonExec(electrumDaemonContainer, "daemon");
    }

    private void daemonStop(ElectrumDaemonContainer<?> electrumDaemonContainer) {
        daemonExec(electrumDaemonContainer, "stop");
    }

    private void loadWalletIfNecessary(ElectrumDaemonContainerConfig config, ElectrumDaemonContainer<?> electrumDaemonContainer, Duration timeout) {
        if (config.getDefaultWallet().isPresent()) {
            try {
                Thread.sleep(timeout.toMillis());

                daemonExec(electrumDaemonContainer, "load_wallet");
            } catch (InterruptedException e) {
                throw new RuntimeException("Error while adapting electrum-daemon: restart with auto-loading wallet failed", e);
            }
        }
    }

    private Container.ExecResult daemonExec(ElectrumDaemonContainer<?> electrumDaemonContainer, String command) {
        try {
            Optional<String> networkFlag = networkFlag(electrumDaemonContainer);

            if (networkFlag.isEmpty()) {
                return electrumDaemonContainer.execInContainer("electrum", command);
            } else {
                if ("daemon".equals(command)) {
                    return electrumDaemonContainer.execInContainer("electrum", networkFlag.get(), command, "-d");
                } else {
                    return electrumDaemonContainer.execInContainer("electrum", networkFlag.get(), command);
                }
            }
        } catch (InterruptedException | IOException e) {
            String errorMessage = String.format("Error while executing `electrum daemon %s`", command);
            throw new RuntimeException(errorMessage, e);
        }
    }

    private void updateElectrumConfigHack(ElectrumDaemonContainer<?> electrumDaemonContainer, String key, String value) {
        try {
            Optional<String> networkFlag = networkFlag(electrumDaemonContainer);

            if (networkFlag.isEmpty()) {
                electrumDaemonContainer.execInContainer("electrum", "--offline", "setconfig", key, value);
            } else {
                electrumDaemonContainer.execInContainer("electrum", "--offline", networkFlag.get(), "setconfig", key, value);
            }
        } catch (InterruptedException | IOException e) {
            String errorMessage = String.format("Error while executing `electrum setconfig %s %s`", key, value);
            throw new RuntimeException(errorMessage, e);
        }
    }
}
