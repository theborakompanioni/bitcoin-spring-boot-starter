package org.tbk.lightning.cln.grpc.config;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.Duration;
import java.util.Objects;

@ConfigurationProperties(
        prefix = "org.tbk.lightning.cln.grpc",
        ignoreUnknownFields = false
)
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
public class ClnClientAutoConfigProperties implements Validator {
    private static final Duration DEFAULT_SHUTDOWN_TIMEOUT = Duration.ofSeconds(10);

    /**
     * Whether the client should be enabled.
     */
    private boolean enabled;

    /**
     * IP address or hostname where cln grpc api is reachable.
     * e.g. localhost, 192.168.0.2, etc.
     */
    private String host;

    /**
     * Port where cln grpc api is listening.
     */
    private int port;

    /**
     * Path to the cert file (e.g. /home/cln/.lightning/regtest/ca.pem).
     */
    private String caCertFilePath;

    /**
     * Path to the client cert file (e.g. /home/cln/.lightning/regtest/client.pem).
     */
    private String clientCertFilePath;

    /**
     * Path to the client key file (e.g. /home/cln/.lightning/regtest/client-key.pem).
     */
    private String clientKeyFilePath;

    private Duration shutdownTimeout;

    public Duration getShutdownTimeout() {
        return Objects.requireNonNullElse(shutdownTimeout, DEFAULT_SHUTDOWN_TIMEOUT);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == ClnClientAutoConfigProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        ClnClientAutoConfigProperties properties = (ClnClientAutoConfigProperties) target;

        if (properties.getPort() <= 0) {
            String errorMessage = String.format("'port' must not be negative - invalid value: %d", properties.getPort());
            errors.rejectValue("port", "port.invalid", errorMessage);
        }

        if (Strings.isNullOrEmpty(properties.getHost())) {
            String errorMessage = String.format("'host' must not be null or empty - invalid value: '%s'", properties.getHost());
            errors.rejectValue("host", "host.invalid", errorMessage);
        }

        if (Strings.isNullOrEmpty(properties.getCaCertFilePath())) {
            String errorMessage = String.format("'caCertFilePath' must not be null or empty - invalid value: '%s'", properties.getHost());
            errors.rejectValue("caCertFilePath", "caCertFilePath.invalid", errorMessage);
        }

        if (Strings.isNullOrEmpty(properties.getClientCertFilePath())) {
            String errorMessage = String.format("'clientCertFilePath' must not be null or empty - invalid value: '%s'", properties.getHost());
            errors.rejectValue("clientCertFilePath", "clientCertFilePath.invalid", errorMessage);
        }

        if (Strings.isNullOrEmpty(properties.getClientKeyFilePath())) {
            String errorMessage = String.format("'clientKeyFilePath' must not be null or empty - invalid value: '%s'", properties.getHost());
            errors.rejectValue("clientKeyFilePath", "clientKeyFilePath.invalid", errorMessage);
        }
    }
}
