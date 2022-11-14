package org.tbk.lightning.lnurl.example;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Optional;

@Data
@ConfigurationProperties(
        prefix = "app",
        ignoreUnknownFields = false
)
class LnurlAuthExampleApplicationProperties {

    private String name;

    private String description;

    private String lnurlAuthBaseUrl;

    public Optional<String> getLnurlAuthBaseUrl() {
        return Optional.ofNullable(lnurlAuthBaseUrl);
    }
}
