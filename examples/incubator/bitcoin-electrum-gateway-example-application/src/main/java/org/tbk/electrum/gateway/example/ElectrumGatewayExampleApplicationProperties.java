package org.tbk.electrum.gateway.example;

import com.google.common.base.Strings;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "my.application")
public class ElectrumGatewayExampleApplicationProperties implements Validator {

    private String destinationAddress;

    private Duration initialDelay = Duration.ZERO;

    private Duration delay = Duration.ofMinutes(30);

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
