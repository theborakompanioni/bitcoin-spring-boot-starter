package org.tbk.lightning.lnd.grpc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Data
@ConfigurationProperties(
        prefix = "org.tbk.lightning.lnd.grpc",
        ignoreUnknownFields = false
)
public class LndJsonRpcClientAutoConfigProperties implements Validator {

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
     * where lnd daemon is reachable
     * e.g. https://localhost:10001
     */
    private String rpchost;

    /**
     * Port where lnd daemon ist listening
     */
    private int rpcport;

    /**
     * Path to the cert file (e.g. /home/lnd/.lnd/tls.cert)
     */
    private String certFilePath;

    /**
     * Path to the cert file (e.g. /home/lnd/.lnd/data/chain/bitcoin/regtest/admin.macaroon)
     */
    private String macaroonFilePath;

    public Network getNetwork() {
        return Optional.ofNullable(network)
                .orElse(Network.mainnet);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == LndJsonRpcClientAutoConfigProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        LndJsonRpcClientAutoConfigProperties properties = (LndJsonRpcClientAutoConfigProperties) target;

        if (properties.getRpcport() < 0) {
            String errorMessage = String.format("Port must not be negative - invalid value: %d", properties.getRpcport());
            errors.rejectValue("rpcport", "rpcport.invalid", errorMessage);
        }

        /*String rpchost = properties.getRpchost();
        if (!Strings.isNullOrEmpty(rpchost)) {
            boolean isHttp = rpchost.startsWith("http://");
            boolean isHttps = rpchost.startsWith("https://");

            boolean validProtocol = isHttp || isHttps;
            if (!validProtocol) {
                String errorMessage = String.format("Host must either start with 'http://' or 'https://' - invalid value: %s", rpchost);
                errors.rejectValue("rpchost", "rpchost.invalid", errorMessage);
            }
        }*/
    }
}
