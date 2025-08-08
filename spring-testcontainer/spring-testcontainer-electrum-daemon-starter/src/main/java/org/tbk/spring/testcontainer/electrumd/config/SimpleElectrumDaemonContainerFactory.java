package org.tbk.spring.testcontainer.electrumd.config;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.FileNameUtils;
import org.tbk.spring.testcontainer.core.CustomHostPortWaitStrategy;
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumd.config.ElectrumDaemonContainerConfig.WalletParams;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.tbk.spring.testcontainer.electrumd.config.ElectrumDaemonContainerProperties.ELECTRUM_NETWORK_ENV_NAME;

@Slf4j
public final class SimpleElectrumDaemonContainerFactory {

    private static final int DEFAULT_RPC_PORT = 7000;

    private static final List<Integer> hardcodedStandardPorts = ImmutableList.<Integer>builder()
            .add(DEFAULT_RPC_PORT)
            .build();

    private static final WaitStrategy containerWaitStrategy = CustomHostPortWaitStrategy.builder()
            .ports(hardcodedStandardPorts)
            .build();

    private static final AtomicLong containerNameIdCounter = new AtomicLong(0L);

    public ElectrumDaemonContainer<?> createStartedElectrumDaemonContainer(ElectrumDaemonContainerConfig config) {
        ImmutableMap.Builder<String, String> environmentBuilder = ImmutableMap.<String, String>builder()
                .putAll(config.getEnvironment());

        config.getServer().ifPresent(serverUrl -> {
            environmentBuilder.put("ELECTRUM_CONFIG_SERVER", serverUrl);
            environmentBuilder.put("ELECTRUM_CONFIG_ONESERVER", "true");
            // electrum says:
            // > `both "oneserver" and "auto_connect" options enabled, disabling "auto_connect" and resetting "server"`
            // and will disable our server if we do not disable auto_connect
            environmentBuilder.put("ELECTRUM_CONFIG_AUTO_CONNECT", "false");
        });

        config.getProxy().ifPresent(proxyParams -> {
            environmentBuilder.put("ELECTRUM_CONFIG_PROXY", proxyParams.getProxy());
            environmentBuilder.put("ELECTRUM_CONFIG_PROXY_USER", proxyParams.getUser().orElse(""));
            environmentBuilder.put("ELECTRUM_CONFIG_PROXY_PASSWORD", proxyParams.getPassword().orElse(""));
        });

        ElectrumDaemonContainer<?> electrumDaemonContainer = new ElectrumDaemonContainer<>(config.getDockerImageName())
                .withCreateContainerCmdModifier(cmdModifier(() -> dockerContainerName(config)))
                .withExposedPorts(hardcodedStandardPorts.toArray(new Integer[]{}))
                .withEnv(environmentBuilder.buildKeepingLast())
                .waitingFor(containerWaitStrategy);

        copyWalletsToContainerIfNecessary(config, electrumDaemonContainer);

        electrumDaemonContainer.start();

        config.getDefaultWallet().ifPresent(it -> {
            // give the daemon some time to startup; 5000ms seems to be enough
            Container.ExecResult execResult = tryLoadWallet(electrumDaemonContainer, it, config.getLoadWalletRetryDelay());
            if (execResult.getExitCode() != 0) {
                log.error("Error while loading default wallet: {}", execResult.getStderr());
                throw new IllegalStateException("Could not load default wallet");
            }
        });

        return electrumDaemonContainer;
    }

    private Consumer<CreateContainerCmd> cmdModifier(Supplier<String> name) {
        return MoreTestcontainers.cmdModifiers().withName(name.get());
    }

    private String dockerContainerName(ElectrumDaemonContainerConfig config) {
        return String.format("%s-%s-%d", config.getDockerImageName().getUnversionedPart(),
                        Integer.toHexString(System.identityHashCode(this)),
                        containerNameIdCounter.getAndIncrement())
                .replace("/", "-");
    }

    private void copyWalletsToContainerIfNecessary(ElectrumDaemonContainerConfig config,
                                                   ElectrumDaemonContainer<?> container) {
        config.getWallets().forEach(walletPath -> {
            tryCopyWalletToContainer(config, container, walletPath);
        });
    }

    private void tryCopyWalletToContainer(ElectrumDaemonContainerConfig config,
                                          ElectrumDaemonContainer<?> container,
                                          String walletPath) {
        try {
            MountableFile mountableWalletFromClasspath = MountableFile.forClasspathResource(walletPath);
            copyWalletToContainer(config, container, mountableWalletFromClasspath);
        } catch (Exception e) {
            // fallback search on host
            MountableFile mountableWalletFromHost = MountableFile.forHostPath(walletPath);
            if (!Files.exists(Path.of(mountableWalletFromHost.getResolvedPath()))) {
                throw new IllegalStateException("Could not find wallet file '%s'".formatted(walletPath));
            }
            copyWalletToContainer(config, container, mountableWalletFromHost);
        }
    }

    private void copyWalletToContainer(ElectrumDaemonContainerConfig config,
                                       ElectrumDaemonContainer<?> container,
                                       MountableFile mountableWallet) {
        String containerWalletFilePath = walletFilePathInContainer(mountableWallet.getResolvedPath(), config);

        if (log.isDebugEnabled()) {
            String filesystemPath = mountableWallet.getFilesystemPath();
            log.debug("copy file to container: {} -> {}", filesystemPath, containerWalletFilePath);
        }

        container.withCopyFileToContainer(mountableWallet, containerWalletFilePath);
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
                log.debug("Could not load wallet '{}' on first try. Will sleep {} and try again...", wallet.getWalletPath(), delay);
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
