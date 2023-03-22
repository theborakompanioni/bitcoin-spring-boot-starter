package org.tbk.lightning.cln.grpc.config;

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
     * IP address or hostname including http:// or https:// where cln grpc api is reachable.
     * e.g. http://localhost, https://192.168.0.2, etc.
     */
    private String host;

    /**
     * Port where cln grpc api is listening.
     */
    private int port;


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

        if (properties.getPort() < 0) {
            String errorMessage = String.format("Port must not be negative - invalid value: %d", properties.getPort());
            errors.rejectValue("port", "port.invalid", errorMessage);
        }
    }
}
