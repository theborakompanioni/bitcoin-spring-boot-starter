package org.tbk.lightning.lnurl.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.Optional;

@ConfigurationProperties(
        prefix = "app",
        ignoreUnknownFields = false
)
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
class LnurlAuthExampleApplicationProperties {

    private String name;

    private String description;

    private String lnurlAuthBaseUrl;

    public Optional<String> getLnurlAuthBaseUrl() {
        return Optional.ofNullable(lnurlAuthBaseUrl);
    }
}
