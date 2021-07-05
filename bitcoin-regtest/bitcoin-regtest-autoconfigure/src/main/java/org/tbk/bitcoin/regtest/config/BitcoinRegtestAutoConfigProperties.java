package org.tbk.bitcoin.regtest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Data
@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.regtest",
        ignoreUnknownFields = false
)
public class BitcoinRegtestAutoConfigProperties implements Validator {

    /**
     * Whether the autoconfig should be enabled
     */
    private boolean enabled;

    private BitcoinRegtestMiningProperties mining;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == BitcoinRegtestAutoConfigProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        BitcoinRegtestAutoConfigProperties properties = (BitcoinRegtestAutoConfigProperties) target;

        if (properties.mining != null) {
            errors.pushNestedPath("mining");
            ValidationUtils.invokeValidator(properties.mining, properties.mining, errors);
            errors.popNestedPath();
        }
    }
}
