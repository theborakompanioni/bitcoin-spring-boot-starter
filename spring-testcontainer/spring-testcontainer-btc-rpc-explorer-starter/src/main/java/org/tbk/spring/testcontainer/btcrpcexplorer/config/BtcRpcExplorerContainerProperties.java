package org.tbk.spring.testcontainer.btcrpcexplorer.config;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.tbk.spring.testcontainer.core.AbstractContainerProperties;
import org.testcontainers.shaded.com.google.common.base.CharMatcher;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = false)
@ConfigurationProperties(
        prefix = "org.tbk.spring.testcontainer.btcrpcexplorer",
        ignoreUnknownFields = false
)
public class BtcRpcExplorerContainerProperties extends AbstractContainerProperties implements Validator {

    private static final Map<String, String> defaultEnvironment = ImmutableMap.<String, String>builder()
            .put("BTCEXP_PRIVACY_MODE", "true")
            .put("BTCEXP_NO_INMEMORY_RPC_CACHE", "true")
            .put("BTCEXP_SLOW_DEVICE_MODE", "false")
            .put("BTCEXP_NO_RATES", "true")
            .put("BTCEXP_BASIC_AUTH_PASSWORD", RandomStringUtils.randomAlphanumeric(32))
            .put("BTCEXP_RPC_ALLOWALL", "false")
            .put("BTCEXP_UI_SHOW_TOOLS_SUBHEADER", "true")
            .put("BTCEXP_DEMO", "false")
            .build();

    // following env vars are not allowed to be overridden by the user as we set them programmatically
    private static final List<String> reservedEnvironment = ImmutableList.<String>builder()
            .add("BTCEXP_HOST")
            .add("BTCEXP_PORT")
            .add("BTCEXP_BITCOIND_HOST")
            .add("BTCEXP_BITCOIND_PORT")
            .add("BTCEXP_BITCOIND_USER")
            .add("BTCEXP_BITCOIND_PASS")
            .add("BTCEXP_ELECTRUMX_SERVERS")
            .build();

    private BitcoindProperties bitcoind;

    private ElectrumxProperties electrumx;

    public BtcRpcExplorerContainerProperties() {
        super(Collections.emptyList(), defaultEnvironment);
    }

    @Override
    public Optional<String> getCommandValueByKey(String key) {
        return Optional.empty();
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == BtcRpcExplorerContainerProperties.class;
    }

    /**
     * Validate the container properties.
     * <p>
     * Keep in mind that Testcontainers splits commands on whitespaces.
     * This means, every property that is part of a command, must not contain whitespaces.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void validate(Object target, Errors errors) {
        BtcRpcExplorerContainerProperties properties = (BtcRpcExplorerContainerProperties) target;

        if (this.bitcoind == null) {
            errors.rejectValue("bitcoind", "bitcoind.invalid", "'bitcoind' must be present");
        } else {
            if (Strings.isNullOrEmpty(this.bitcoind.getRpchost())) {
                String errorMessage = "'rpchost' must not be empty";
                errors.rejectValue("bitcoind", "bitcoind.invalid", errorMessage);
            }
            if (this.bitcoind.getRpcport() <= 0) {
                String errorMessage = String.format("'rpcport' is invalid - given: %d", this.bitcoind.getRpcport());
                errors.rejectValue("bitcoind", "bitcoind.invalid", errorMessage);
            }
            if (Strings.isNullOrEmpty(this.bitcoind.getRpcuser())) {
                String errorMessage = "'rpcuser' must not be empty";
                errors.rejectValue("bitcoind", "bitcoind.invalid", errorMessage);
            }
        }

        if (this.getElectrumx() != null) {
            ElectrumxProperties electrumxProperties = this.getElectrumx();
            if (Strings.isNullOrEmpty(electrumxProperties.getRpchost())) {
                String errorMessage = "'rpchost' entry must not be empty";
                errors.rejectValue("electrumx", "electrumx.invalid", errorMessage);
            }
            if (electrumxProperties.getTcpport() <= 0) {
                String errorMessage = String.format("'tcpport' is invalid - given: %d", electrumxProperties.getTcpport());
                errors.rejectValue("electrumx", "electrumx.invalid", errorMessage);
            }
        }

        Map<String, String> environment = properties.getEnvironment();

        reservedEnvironment.forEach(it -> {
            String envValueOrNull = environment.get(it);
            if (envValueOrNull != null) {
                String errorMessage = String.format("'%s' env var is not allowed", it);
                errors.rejectValue("environment", "environment.invalid", errorMessage);
            }
        });
    }

    @Data
    public static class BitcoindProperties {
        /**
         * IP address or hostname (without http:// or https://)
         * where bitcoin daemon is reachable
         * e.g. localhost, 192.168.0.2, etc.
         */
        private String rpchost;

        /**
         * Port where bitcoin daemon is listening
         */
        private int rpcport;

        /**
         * RPC username
         */
        private String rpcuser;

        /**
         * RPC password
         */
        private String rpcpass;
    }

    @Data
    public static class ElectrumxProperties {
        /**
         * IP address or hostname (without http:// or https://)
         * where ElectrumX is reachable
         * e.g. localhost, 192.168.0.2, etc.
         */
        private String rpchost;

        /**
         * Port where ElectrumX is listening
         */
        private int tcpport;
    }
}

