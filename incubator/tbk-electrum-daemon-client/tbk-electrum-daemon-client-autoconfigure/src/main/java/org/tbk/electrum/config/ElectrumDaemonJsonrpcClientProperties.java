package org.tbk.electrum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Properties are named according to electrum config file.
 * <p>
 * e.g.
 * {
 * "enabled": true,
 * "rpchost": "0.0.0.0",
 * "rpcport": 7000,
 * "rpcuser": "myrpcuser",
 * "rpcpassword": "correct horse battery staple"
 * }
 */
@Data
@ConfigurationProperties(prefix = "org.tbk.bitcoin.electrum-daemon.jsonrpc")
public class ElectrumDaemonJsonrpcClientProperties implements Validator {

    /**
     * Whether a client should be enabled
     */
    private boolean enabled;

    /**
     * IP address or hostname including http:// or https://
     * where electrum daemon is reachable
     * e.g. http://localhost, https://192.168.0.2, etc.
     */
    private String rpchost;

    /**
     * Port where electrum daemon is listening
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

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == ElectrumDaemonJsonrpcClientProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {

    }
}
