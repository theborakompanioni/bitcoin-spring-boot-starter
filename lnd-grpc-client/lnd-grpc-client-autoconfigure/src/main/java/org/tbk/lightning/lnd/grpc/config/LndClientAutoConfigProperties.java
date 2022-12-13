package org.tbk.lightning.lnd.grpc.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Objects;
import java.util.Optional;

@ConfigurationProperties(
        prefix = "org.tbk.lightning.lnd.grpc",
        ignoreUnknownFields = false
)
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
public class LndClientAutoConfigProperties implements Validator {

    public enum Network {
        mainnet,
        testnet,
        regtest;
    }

    /**
     * Whether the client should be enabled.
     */
    private boolean enabled;

    private Network network;

    /**
     * IP address or hostname including http:// or https:// where lnd daemon is reachable.
     * e.g. http://localhost, https://192.168.0.2, etc.
     */
    private String rpchost;

    /**
     * Port where lnd daemon is listening.
     */
    private int rpcport;

    /**
     * Path to the cert file (e.g. /home/lnd/.lnd/tls.cert).
     */
    private String certFilePath;

    /**
     * Path to the cert file (e.g. /home/lnd/.lnd/data/chain/bitcoin/regtest/admin.macaroon)
     */
    private String macaroonFilePath;

    public Network getNetwork() {
        return Objects.requireNonNullElse(network, Network.mainnet);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == LndClientAutoConfigProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        LndClientAutoConfigProperties properties = (LndClientAutoConfigProperties) target;

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
