package org.tbk.bitcoin.tool.cryptoscamdb.config;

import com.google.common.base.Strings;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;


@Data
@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.tool.cryptoscamdb",
        ignoreUnknownFields = false
)
public class CryptoScamDbAutoConfigProperties implements Validator {
    private static final String DEFAULT_VERSION = Optional.ofNullable(CryptoScamDbAutoConfigProperties.class
            .getPackage()
            .getImplementationVersion()
    ).orElse("0.0.0");
    private static final String DEFAULT_USERAGENT = "tbk-cryptoscamdb-client/" + DEFAULT_VERSION;

    private static final String DEFAULT_BASE_URL = "https://api.cryptoscamdb.org";

    /**
     * Whether the client should be enabled
     */
    private boolean enabled;

    private String baseUrl;
    private String userAgent;

    public String getBaseUrl() {
        return Optional.ofNullable(baseUrl)
                .orElse(DEFAULT_BASE_URL);
    }

    public String getUserAgent() {
        return Optional.ofNullable(userAgent)
                .orElse(DEFAULT_USERAGENT);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == CryptoScamDbAutoConfigProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        CryptoScamDbAutoConfigProperties properties = (CryptoScamDbAutoConfigProperties) target;

        String baseUrlValue = properties.getBaseUrl();
        if (!Strings.isNullOrEmpty(baseUrlValue)) {
            boolean isHttp = baseUrlValue.startsWith("http://");
            boolean isHttps = baseUrlValue.startsWith("https://");

            boolean validProtocol = isHttp || isHttps;
            if (!validProtocol) {
                String errorMessage = String.format("'baseUrl' must either start with 'http://' or 'https://' - invalid value: %d", baseUrlValue);
                errors.rejectValue("baseUrl", "baseUrl.invalid", errorMessage);
            }
        }
    }
}
