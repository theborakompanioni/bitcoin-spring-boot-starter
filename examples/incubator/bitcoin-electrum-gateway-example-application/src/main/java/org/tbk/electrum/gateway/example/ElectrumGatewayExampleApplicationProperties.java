package org.tbk.electrum.gateway.example;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.Duration;
import java.util.Objects;

@ConfigurationProperties(prefix = "my.application")
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
public class ElectrumGatewayExampleApplicationProperties implements Validator {

    private static final Duration DEFAULT_INITIAL_DELAY = Duration.ZERO;

    private static final Duration DEFAULT_DELAY = Duration.ofMinutes(30);

    private String destinationAddress;

    private Duration initialDelay;

    private Duration delay;

    public Duration getInitialDelay() {
        return Objects.requireNonNullElse(initialDelay, DEFAULT_INITIAL_DELAY);
    }

    public Duration getDelay() {
        return Objects.requireNonNullElse(delay, DEFAULT_DELAY);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == ElectrumGatewayExampleApplicationProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        ElectrumGatewayExampleApplicationProperties properties = (ElectrumGatewayExampleApplicationProperties) target;

        if (Strings.isNullOrEmpty(properties.getDestinationAddress())) {
            String errorMessage = String.format("'destinationAddress' must not be empty - invalid value: %s", properties.getDestinationAddress());
            errors.rejectValue("destinationAddress", "destinationAddress.invalid", errorMessage);
        }

        if (initialDelay.isNegative()) {
            String errorMessage = String.format("'initialDelay' must not be less than zero - invalid value: %s", properties.getInitialDelay());
            errors.rejectValue("initialDelay", "initialDelay.invalid", errorMessage);
        }
        if (delay.isNegative() || delay.isZero()) {
            String errorMessage = String.format("'delay' must not be less than or equal to zero - invalid value: %s", properties.getDelay());
            errors.rejectValue("delay", "delay.invalid", errorMessage);
        }
    }
}
