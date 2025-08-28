package org.tbk.spring.testcontainer.electrumd.config;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import org.testcontainers.utility.DockerImageName;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;
import static org.tbk.spring.testcontainer.electrumd.config.ElectrumDaemonContainerProperties.ELECTRUM_NETWORK_ENV_NAME;

@Value
@Builder(toBuilder = true)
public class ElectrumDaemonContainerConfig {

    // currently only the image from "theborakompanioni" is supported
    static final String DEFAULT_DOCKER_IMAGE_NAME = "ghcr.io/theborakompanioni/electrum-daemon:4.6.2@sha256:b214fa9a30cb260a99daa88a5880abf110bda1556a359a1706d3e2112e4b088a";
    static final DockerImageName defaultDockerImageName = DockerImageName.parse(DEFAULT_DOCKER_IMAGE_NAME);

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

    @Nullable
    String server;

    @Nullable
    ProxyParams proxy;

    @Nullable
    DockerImageName dockerImageName;

    /**
     * A delay to retry loading a wallet if the first attempt failed.
     * This provides some time to the daemon to start before trying to load a wallet again.
     * 5 seconds seems to be enough. Increase on demand.
     */
    @Builder.Default
    Duration loadWalletRetryDelay = Duration.ofSeconds(5);

    public Optional<WalletParams> getDefaultWallet() {
        return Optional.ofNullable(defaultWallet);
    }

    public List<String> getWallets() {
        return Collections.unmodifiableList(requireNonNullElseGet(wallets, Collections::emptyList));
    }

    public Optional<String> getServer() {
        return Optional.ofNullable(server);
    }

    public Optional<ProxyParams> getProxy() {
        return Optional.ofNullable(proxy);
    }

    public DockerImageName getDockerImageName() {
        return requireNonNullElse(dockerImageName, defaultDockerImageName);
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

    @Value
    @Builder
    public static class ProxyParams {
        @NonNull
        String proxy;

        @Nullable
        String user;

        @Nullable
        String password;

        public Optional<String> getUser() {
            return Optional.ofNullable(user);
        }

        public Optional<String> getPassword() {
            return Optional.ofNullable(password);
        }
    }
}
