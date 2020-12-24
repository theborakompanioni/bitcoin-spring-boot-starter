package org.tbk.electrum.gateway.example;

import com.google.common.base.Strings;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


@Data
@ConfigurationProperties(prefix = "my.application")
public class ElectrumGatewayExampleApplicationProperties implements Validator {

    private String destinationAddress;

    private int initialDelay;

    private int delay = 30;

    private String timeUnit = TimeUnit.MINUTES.name();

    public TimeUnit getTimeUnitOrThrow() {
        try {
            return TimeUnit.valueOf(timeUnit);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("'timeUnit' must represent a valid TimeUnit", e);
        }
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

        if (initialDelay < 0) {
            String errorMessage = String.format("'initialDelay' must not be less than zero - invalid value: %d", properties.getInitialDelay());
            errors.rejectValue("initialDelay", "initialDelay.invalid", errorMessage);
        }
        if (delay <= 0) {
            String errorMessage = String.format("'delay' must not be less than or equal to zero - invalid value: %d", properties.getDelay());
            errors.rejectValue("delay", "delay.invalid", errorMessage);
        }

        boolean validTimeUnit = Arrays.stream(TimeUnit.values())
                .map(Enum::name)
                .anyMatch(it -> it.equals(properties.getTimeUnit()));

        if (!validTimeUnit) {
            String errorMessage = String.format("'timeUnit' must represent a valid TimeUnit (MINUTES, HOURS, etc.) - invalid value: %s", properties.getTimeUnit());
            errors.rejectValue("timeUnit", "timeUnit.invalid", errorMessage);
        }
    }
}
