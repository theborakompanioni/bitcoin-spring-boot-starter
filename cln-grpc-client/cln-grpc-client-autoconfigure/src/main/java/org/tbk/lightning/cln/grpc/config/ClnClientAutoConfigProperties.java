package org.tbk.lightning.cln.grpc.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.util.StringUtils;
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
     * Path to the CA cert file (e.g. /home/cln/.lightning/regtest/ca.pem).
     */
    private String caCertFilePath;

    /**
     * CA cert encoded in base64.
     */
    private String caCertBase64;

    /**
     * Path to the client cert file (e.g. /home/cln/.lightning/regtest/client.pem).
     */
    private String clientCertFilePath;

    /**
     * Client cert encoded in base64.
     */
    private String clientCertBase64;

    /**
     * Path to the client key file (e.g. /home/cln/.lightning/regtest/client-key.pem).
     */
    private String clientKeyFilePath;

    /**
     * Client key encoded in base64.
     */
    private String clientKeyBase64;

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

        if (!StringUtils.hasText(properties.getHost())) {
            String errorMessage = String.format("'host' must not be empty - invalid value: '%s'", properties.getHost());
            errors.rejectValue("host", "host.invalid", errorMessage);
        }

        boolean caCertFileAbsent = !StringUtils.hasText(properties.getCaCertFilePath());
        boolean caCertRawAbsent = !StringUtils.hasText(properties.getCaCertBase64());
        if (caCertFileAbsent && caCertRawAbsent) {
            String errorMessage = "'caCert' must not be empty: Either provide a path or a base64-encoded value";
            errors.rejectValue("caCertFilePath", "caCertFilePath.invalid", errorMessage);
        }

        boolean clientCertFileAbsent = !StringUtils.hasText(properties.getClientCertFilePath());
        boolean clientCertRawAbsent = !StringUtils.hasText(properties.getClientCertBase64());
        if (clientCertFileAbsent && clientCertRawAbsent) {
            String errorMessage = "'clientCert' must not be empty: Either provide a path or a base64-encoded value";
            errors.rejectValue("clientCertFilePath", "clientCertFilePath.invalid", errorMessage);
        }

        boolean clientKeyFileAbsent = !StringUtils.hasText(properties.getClientKeyFilePath());
        boolean clientKeyRawAbsent = !StringUtils.hasText(properties.getClientKeyBase64());
        if (clientKeyFileAbsent && clientKeyRawAbsent) {
            String errorMessage = "'clientKey' must not be empty: Either provide a path or a base64-encoded value";
            errors.rejectValue("clientKeyFilePath", "clientKeyFilePath.invalid", errorMessage);
        }
    }
}
