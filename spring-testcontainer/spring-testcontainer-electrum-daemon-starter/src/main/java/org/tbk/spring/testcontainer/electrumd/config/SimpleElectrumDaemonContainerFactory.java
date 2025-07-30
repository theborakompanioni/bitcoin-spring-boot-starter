package org.tbk.spring.testcontainer.electrumd.config;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.FileNameUtils;
import org.tbk.spring.testcontainer.core.CustomHostPortWaitStrategy;
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumd.config.SimpleElectrumDaemonContainerFactory.ElectrumDaemonContainerConfig.WalletParams;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNullElseGet;
import static org.tbk.spring.testcontainer.core.MoreTestcontainers.buildInternalContainerUrlWithoutProtocol;
import static org.tbk.spring.testcontainer.electrumd.config.ElectrumDaemonContainerProperties.ELECTRUM_NETWORK_ENV_NAME;

@Slf4j
public final class SimpleElectrumDaemonContainerFactory {

    @Value
    @Builder(toBuilder = true)
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

        @Nullable
        WalletParams defaultWallet;

        @Singular("addWallet")
        List<String> wallets;

        public Optional<WalletParams> getDefaultWallet() {
            return Optional.ofNullable(defaultWallet);
        }

        public List<String> getWallets() {
            return Collections.unmodifiableList(requireNonNullElseGet(wallets, Collections::emptyList));
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


        @Value
        @Builder
        public static class WalletParams {
            @NonNull
            String walletPath;

            @Nullable
            String password;

            public Optional<String> getPassword() {
                return Optional.ofNullable(password);
            }
        }
    }

    // currently only the image from "theborakompanioni" is supported
    private static final String DOCKER_IMAGE_NAME = "ghcr.io/theborakompanioni/electrum-daemon:4.6.0.1@sha256:1e97f069ea9053f7d4c922dfdeac7336e444f4f2933a24662a3842dba3157de5";

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

        copyWalletsToContainerIfNecessary(config, electrumDaemonContainer);

        electrumDaemonContainer.start();

        config.getDefaultWallet().ifPresent(it -> {
            // give the daemon some time to startup; 5000ms seems to be enough
            Container.ExecResult execResult = tryLoadWallet(electrumDaemonContainer, it, Duration.ofMillis(5_000));
            if (execResult.getExitCode() != 0) {
                log.error("Error while loading default wallet: {}", execResult.getStderr());
                throw new IllegalStateException("Could not load default wallet");
            }
        });

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

    private void copyWalletsToContainerIfNecessary(ElectrumDaemonContainerConfig config,
                                                   ElectrumDaemonContainer<?> container) {
        config.getWallets().forEach(wallet -> {
            MountableFile mountableWallet = MountableFile.forClasspathResource(wallet);
            String containerWalletFilePath = walletFilePathInContainer(wallet, config);

            if (log.isDebugEnabled()) {
                String filesystemPath = mountableWallet.getFilesystemPath();
                log.debug("copy file to container: {} -> {}", filesystemPath, containerWalletFilePath);
            }

            container.withCopyFileToContainer(mountableWallet, containerWalletFilePath);
        });
    }

    private String walletFilePathInContainer(String walletFileName, ElectrumDaemonContainerConfig config) {
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

        String fileNameExtensionOrEmpty = Optional.ofNullable(FileNameUtils.getExtension(walletFileName))
                .filter(it -> !it.isBlank())
                .map(".%s"::formatted)
                .orElse("");
        String fileName = "%s%s".formatted(
                FileNameUtils.getBaseName(walletFileName),
                fileNameExtensionOrEmpty
        );

        return "%s/%s".formatted(networkWalletDir, fileName);
    }

    private Optional<String> networkFlag(ElectrumDaemonContainer<?> container) {
        return Optional.of(container.getEnvMap())
                .map(it -> it.get(ELECTRUM_NETWORK_ENV_NAME))
                .map(it -> "--" + it);
    }

    private Container.ExecResult tryLoadWallet(ElectrumDaemonContainer<?> container, WalletParams wallet, Duration delay) {
        Container.ExecResult execResult = tryLoadWallet(container, wallet);
        if (execResult.getExitCode() != 0) {
            // try again with given delay if first try did not work
            try {
                Thread.sleep(delay.toMillis());
                return tryLoadWallet(container, wallet);
            } catch (InterruptedException ie) {
                throw new RuntimeException("Error while adapting electrum-daemon: restart with auto-loading default wallet failed", ie);
            }
        }
        return execResult;
    }

    private Container.ExecResult tryLoadWallet(ElectrumDaemonContainer<?> container, WalletParams wallet) {
        String[] commands = Stream.concat(
                Stream.of(
                        // NOTE: there is no space between the "w" arg - otherwise electrum throws "no such file" error
                        "-w%s".formatted(wallet.getWalletPath()),
                        "load_wallet"
                ),
                wallet.getPassword().map("--password %s"::formatted).stream()
        ).toArray(String[]::new);
        return daemonExec(container, commands);
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
