package org.tbk.xchange.spring.config;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Collections;
import java.util.Map;

@Data
@ConfigurationProperties(
        prefix = "org.tbk.xchange",
        ignoreUnknownFields = false
)
public class XChangeAutoConfigProperties implements Validator {

    private boolean enabled;

    private Map<String, ExchangeSpecificationProperties> specifications;

    public Map<String, ExchangeSpecificationProperties> getSpecifications() {
        return this.specifications != null ? ImmutableMap.copyOf(specifications) : Collections.emptyMap();
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == XChangeAutoConfigProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        XChangeAutoConfigProperties properties = (XChangeAutoConfigProperties) target;

        properties.getSpecifications().forEach((key, value) -> {
            errors.pushNestedPath("specifications[" + key + "]");
            ValidationUtils.invokeValidator(value, value, errors);
            errors.popNestedPath();
        });
    }
}
