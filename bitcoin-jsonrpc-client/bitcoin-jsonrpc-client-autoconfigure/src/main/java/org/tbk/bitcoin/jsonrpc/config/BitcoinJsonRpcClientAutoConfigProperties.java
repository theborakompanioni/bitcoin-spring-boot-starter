package org.tbk.bitcoin.jsonrpc.config;

import com.google.common.base.Strings;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
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
        prefix = "org.tbk.bitcoin.jsonrpc",
        ignoreUnknownFields = false
)
public class BitcoinJsonRpcClientAutoConfigProperties implements Validator {

    public enum Network {
        mainnet,
        testnet,
        regtest;
    }

    /**
     * Whether the client should be enabled
     */
    private boolean enabled;

    private Network network;

    /**
     * IP address or hostname including http:// or https://
     * where bitcoin daemon is reachable
     * e.g. http://localhost, https://192.168.0.2, etc.
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
    private String rpcpassword;


    public Network getNetwork() {
        return Optional.ofNullable(network)
                .orElse(Network.mainnet);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == BitcoinJsonRpcClientAutoConfigProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        BitcoinJsonRpcClientAutoConfigProperties properties = (BitcoinJsonRpcClientAutoConfigProperties) target;

        if (properties.getRpcport() < 0) {
            String errorMessage = String.format("Port must not be negative - invalid value: %d", properties.getRpcport());
            errors.rejectValue("rpcport", "rpcport.invalid", errorMessage);
        }

        String rpchost = properties.getRpchost();
        if (!Strings.isNullOrEmpty(rpchost)) {
            boolean isHttp = rpchost.startsWith("http://");
            boolean isHttps = rpchost.startsWith("https://");

            boolean validProtocol = isHttp || isHttps;
            if (!validProtocol) {
                String errorMessage = String.format("Host must either start with 'http://' or 'https://' - invalid value: %s", rpchost);
                errors.rejectValue("rpchost", "rpchost.invalid", errorMessage);
            }
        }
    }
}
