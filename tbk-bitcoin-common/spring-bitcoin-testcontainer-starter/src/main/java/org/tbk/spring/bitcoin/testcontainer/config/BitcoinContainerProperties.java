package org.tbk.spring.bitcoin.testcontainer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Data
@ConfigurationProperties(
        prefix = "org.tbk.spring.bitcoin.testcontainer",
        ignoreUnknownFields = false
)
public class BitcoinContainerProperties implements Validator {

    private boolean enabled;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == BitcoinContainerProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        BitcoinContainerProperties properties = (BitcoinContainerProperties) target;

    }
}

