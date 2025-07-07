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
import java.util.stream.Stream;

import static org.tbk.spring.testcontainer.core.MoreTestcontainers.buildInternalContainerUrlWithoutProtocol;
import static org.tbk.spring.testcontainer.electrumd.config.ElectrumDaemonContainerProperties.ELECTRUM_NETWORK_ENV_NAME;

@Slf4j
public final class SimpleElectrumDaemonContainerFactory {

    @Value
    @Builder
    public static class ElectrumDaemonContainerConfig {
        private static final Map<String, String> defaultEnvironment = ImmutableMap.<String, String>builder()
                .put(ELECTRUM_NETWORK_ENV_NAME, "regtest")
                .put("ELECTRUM_CONFIG_AUTO_CONNECT", "true")
                .put("ELECTRUM_CONFIG_LOG_TO_FILE", "true")
                .put("ELECTRUM_CONFIG_CHECK_UPDATES", "false")
                .put("ELECTRUM_CONFIG_DONT_SHOW_TESTNET_WARNING", "true")
                .build();

        @Singular("addEnvVar")
        Map<String, String> environment;

        String defaultWallet;

        public Optional<String> getDefaultWallet() {
            return Optional.ofNullable(defaultWallet);
        }

        public Map<String, String> getEnvironment() {
            return ImmutableMap.<String, String>builder()
                    .putAll(defaultEnvironment)
                    .putAll(environment)
                    .buildKeepingLast();
        }

        public String getNetwork() {
            return environment.getOrDefault(ELECTRUM_NETWORK_ENV_NAME, defaultEnvironment.get(ELECTRUM_NETWORK_ENV_NAME));
        }
    }

    // currently only the image from "theborakompanioni" is supported
    private static final String DOCKER_IMAGE_NAME = "ghcr.io/theborakompanioni/electrum-daemon:4.6.0b1@sha256:70fff910f624909edb97c7af4cfe1768b7fecd198356a178d8b346eee30bf7be";

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
        ImmutableMap.Builder<String, String> environmentBuilder = ImmutableMap.<String, String>builder()
                .putAll(config.getEnvironment());

        serverUrlSupplier.get().ifPresent(serverUrl -> {
            environmentBuilder.put("ELECTRUM_CONFIG_SERVER", serverUrlSupplier.get().orElse("empty"));
            environmentBuilder.put("ELECTRUM_CONFIG_ONESERVER", "true");
            // electrum says:
            // > `both "oneserver" and "auto_connect" options enabled, disabling "auto_connect" and resetting "server"`
            // and will disable our server if we do not disable auto_connect
            environmentBuilder.put("ELECTRUM_CONFIG_AUTO_CONNECT", "false");
        });

        ElectrumDaemonContainer<?> electrumDaemonContainer = new ElectrumDaemonContainer<>(dockerImageName)
                .withCreateContainerCmdModifier(cmdModifier())
                .withExposedPorts(hardcodedStandardPorts.toArray(new Integer[]{}))
                .withEnv(environmentBuilder.buildKeepingLast())
                .waitingFor(containerWaitStrategy);

        copyWalletToContainerIfNecessary(config, electrumDaemonContainer);

        electrumDaemonContainer.start();

        // let the daemon some time to startup; 5000ms seems to be enough
        loadWalletIfNecessary(config, electrumDaemonContainer, Duration.ofMillis(5_000));

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

    private void copyWalletToContainerIfNecessary(ElectrumDaemonContainerConfig config,
                                                  ElectrumDaemonContainer<?> container) {
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

            container.withCopyFileToContainer(mountableWallet, containerWalletFilePath);
        }
    }

    private Optional<String> networkFlag(ElectrumDaemonContainer<?> container) {
        return Optional.of(container.getEnvMap())
                .map(it -> it.get(ELECTRUM_NETWORK_ENV_NAME))
                .filter(it -> !"mainnet".equals(it))
                .map(it -> "--" + it);
    }

    private void loadWalletIfNecessary(ElectrumDaemonContainerConfig config, ElectrumDaemonContainer<?> container, Duration timeout) {
        if (config.getDefaultWallet().isPresent()) {
            try {
                Thread.sleep(timeout.toMillis());

                daemonExec(container, "load_wallet");
            } catch (InterruptedException e) {
                throw new RuntimeException("Error while adapting electrum-daemon: restart with auto-loading wallet failed", e);
            }
        }
    }

    private Container.ExecResult daemonExec(ElectrumDaemonContainer<?> container, String... commands) {
        Optional<String> networkFlag = networkFlag(container);

        String[] command = Stream.concat(Stream.of("electrum", networkFlag.orElse("")), Stream.of(commands))
                .filter(it -> !it.isEmpty())
                .toArray(String[]::new);

        try {
            return container.execInContainer(command);
        } catch (InterruptedException | IOException e) {
            String errorMessage = String.format("Error while executing `%s`", String.join(" ", command));
            throw new RuntimeException(errorMessage, e);
        }
    }
}
