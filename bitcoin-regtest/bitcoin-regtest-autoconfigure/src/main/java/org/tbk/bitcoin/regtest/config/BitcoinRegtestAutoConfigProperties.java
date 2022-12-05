package org.tbk.bitcoin.regtest.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Optional;

@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.regtest",
        ignoreUnknownFields = false
)
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
public class BitcoinRegtestAutoConfigProperties implements Validator {

    /**
     * Whether the autoconfig should be enabled.
     */
    private boolean enabled;

    private BitcoinRegtestMiningProperties mining;

    public Optional<BitcoinRegtestMiningProperties> getMining() {
        return Optional.ofNullable(mining);
    }

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
