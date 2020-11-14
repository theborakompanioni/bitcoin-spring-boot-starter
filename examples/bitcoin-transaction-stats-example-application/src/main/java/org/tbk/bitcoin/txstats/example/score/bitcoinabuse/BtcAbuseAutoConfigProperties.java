package org.tbk.bitcoin.txstats.example.score.bitcoinabuse;

import com.google.common.base.Strings;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;


@Data
@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.tool.btcabuse",
        ignoreUnknownFields = false
)
public class BtcAbuseAutoConfigProperties implements Validator {
    private static final String DEFAULT_BASE_URL = "https://www.bitcoinabuse.com";

    /**
     * Whether the client should be enabled
     */
    private boolean enabled;

    private String baseUrl;
    private String apiToken;

    public String getBaseUrl() {
        return Optional.ofNullable(baseUrl)
                .orElse(DEFAULT_BASE_URL);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == BtcAbuseAutoConfigProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        BtcAbuseAutoConfigProperties properties = (BtcAbuseAutoConfigProperties) target;

        if (Strings.isNullOrEmpty(properties.getApiToken())) {
            String errorMessage = String.format("'apiToken' must not be empty - invalid value: %d", properties.getApiToken());
            errors.rejectValue("apiToken", "apiToken.invalid", errorMessage);
        }

        String baseUrlValue = properties.getBaseUrl();
        if (!Strings.isNullOrEmpty(baseUrlValue)) {
            boolean isHttp = baseUrlValue.startsWith("http://");
            boolean isHttps = baseUrlValue.startsWith("https://");

            boolean validProtocol = isHttp || isHttps;
            if (!validProtocol) {
                String errorMessage = String.format("'baseUrl'' must either start with 'http://' or 'https://' - invalid value: %d", baseUrlValue);
                errors.rejectValue("base", "base.invalid", errorMessage);
            }
        }
    }
}
