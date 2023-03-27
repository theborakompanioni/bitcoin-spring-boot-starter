package org.tbk.lightning.lnd.grpc.config;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Objects;

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
        regtest
    }

    /**
     * Whether the client should be enabled.
     */
    private boolean enabled;

    private Network network;

    /**
     * IP address or hostname where lnd daemon is reachable.
     * e.g. localhost, 192.168.0.2, etc.
     */
    private String host;

    /**
     * Port where lnd daemon is listening.
     */
    private int port;

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

        if (properties.getPort() <= 0) {
            String errorMessage = String.format("Invalid 'port' value: %d", properties.getPort());
            errors.rejectValue("port", "port.invalid", errorMessage);
        }

        if (Strings.isNullOrEmpty(properties.getHost())) {
            String errorMessage = String.format("Invalid `host` value: '%s'", properties.getHost());
            errors.rejectValue("host", "host.invalid", errorMessage);
        }
    }
}
