package org.tbk.bitcoin.btcabuse.config;

import com.google.common.base.Strings;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;


@Data
@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.tool.btcabuse.client",
        ignoreUnknownFields = false
)
public class BtcAbuseClientAutoConfigProperties implements Validator {
    private static final String DEFAULT_VERSION = Optional.ofNullable(
            BtcAbuseClientAutoConfigProperties.class.getPackage().getImplementationVersion()
    ).orElse("0.0.0");

    private static final String DEFAULT_USERAGENT = "tbk-btcabuse-client/" + DEFAULT_VERSION;

    private static final String DEFAULT_BASE_URL = "https://www.bitcoinabuse.com";

    /**
     * Whether the client should be enabled.
     */
    private boolean enabled;

    private String baseUrl;
    private String apiToken;
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
        return clazz == BtcAbuseClientAutoConfigProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        BtcAbuseClientAutoConfigProperties properties = (BtcAbuseClientAutoConfigProperties) target;

        if (Strings.isNullOrEmpty(properties.getApiToken())) {
            String errorMessage = String.format("'apiToken' must not be empty - invalid value: %s", properties.getApiToken());
            errors.rejectValue("apiToken", "apiToken.invalid", errorMessage);
        }

        String baseUrlValue = properties.getBaseUrl();
        if (!Strings.isNullOrEmpty(baseUrlValue)) {
            boolean isHttp = baseUrlValue.startsWith("http://");
            boolean isHttps = baseUrlValue.startsWith("https://");

            boolean validProtocol = isHttp || isHttps;
            if (!validProtocol) {
                String errorMessage = String.format("'baseUrl' must either start with 'http://' or 'https://' - invalid value: %s", baseUrlValue);
                errors.rejectValue("baseUrl", "baseUrl.invalid", errorMessage);
            }
        }
    }
}
