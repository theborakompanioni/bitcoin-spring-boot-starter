package org.tbk.bitcoin.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Optional;

/**
 * {
 * "enabled": true,
 * "rpchost": "0.0.0.0",
 * "rpcport": 7000,
 * "rpcuser": "myrpcuser",
 * "rpcpassword": "correct horse battery staple"
 * }
 */
@Data
@ConfigurationProperties(
        prefix = "org.tbk.bitcoin",
        ignoreUnknownFields = false
)
public class BitcoinClientAutoConfigProperties {
    /**
     * Whether the service should be enabled
     */
    private boolean enabled;

    private BitcoinClientProperties client;

    public Optional<BitcoinClientProperties> getClientOrEmpty() {
        return Optional.ofNullable(client)
                .filter(BitcoinClientProperties::isEnabled);
    }

    @Data
    public static class BitcoinClientProperties implements Validator {
        public enum Network {
            mainnet,
            testnet,
            regtest;
        }

        /**
         * Whether the client should be enabled
         */
        private boolean enabled;

        private String network;

        /**
         * IP address or hostname including http:// or https://
         * where bitcoin daemon is reachable
         * e.g. https://localhost:8332
         */
        private String rpchost;

        /**
         * Port where bitcoin daemon ist listening
         */
        private int rpcport;

        /**
         * RPC username
         */
        private String rpcuser;

        /**
         * RPC password
         */
        private String rpcpassword;


        public Network getNetwork() {
            return Network.valueOf(this.network);
        }

        @Override
        public boolean supports(Class<?> clazz) {
            return clazz == BitcoinClientProperties.class;
        }

        @Override
        public void validate(Object target, Errors errors) {
            ValidationUtils.rejectIfEmpty(errors, "destinationAddress", "destinationAddress.empty");

            BitcoinClientProperties properties = (BitcoinClientProperties) target;

            if (properties.getRpcport() < 0) {
                String errorMessage = String.format("Port must not be negative - invalid value: %d", properties.getRpcport());
                errors.rejectValue("rpcport", "rpcport.invalid", errorMessage);
            }
        }
    }
}
