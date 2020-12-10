package org.tbk.bitcoin.tool.fee.config;

import com.google.common.base.Strings;
import lombok.Data;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Data
public abstract class AbstractFeeClientAutoConfigProperties implements Validator {

    private boolean enabled;

    private String baseUrl;

    private String token;

    protected abstract String getDefaultBaseUrl();

    public String getBaseUrl() {
        return Optional.ofNullable(baseUrl)
                .orElseGet(this::getDefaultBaseUrl);
    }

    public Optional<String> getToken() {
        return Optional.ofNullable(token);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return AbstractFeeClientAutoConfigProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AbstractFeeClientAutoConfigProperties properties = (AbstractFeeClientAutoConfigProperties) target;

        String baseUrl = properties.getBaseUrl();
        if (!Strings.isNullOrEmpty(baseUrl)) {
            boolean isHttp = baseUrl.startsWith("http://");
            boolean isHttps = baseUrl.startsWith("https://");

            boolean validProtocol = isHttp || isHttps;
            if (!validProtocol) {
                String errorMessage = String.format("'baseUrl' must either start with 'http://' or 'https://' - invalid value: %s", baseUrl);
                errors.rejectValue("baseUrl", "baseUrl.invalid", errorMessage);
            }
        }
    }
}
