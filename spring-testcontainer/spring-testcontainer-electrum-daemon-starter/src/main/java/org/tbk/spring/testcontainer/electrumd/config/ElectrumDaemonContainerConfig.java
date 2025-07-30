package org.tbk.spring.testcontainer.electrumd.config;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNullElseGet;
import static org.tbk.spring.testcontainer.electrumd.config.ElectrumDaemonContainerProperties.ELECTRUM_NETWORK_ENV_NAME;

@Value
@Builder(toBuilder = true)
public class ElectrumDaemonContainerConfig {
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
