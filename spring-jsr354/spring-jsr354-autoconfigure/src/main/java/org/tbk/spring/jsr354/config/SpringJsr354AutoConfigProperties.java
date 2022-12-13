package org.tbk.spring.jsr354.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Objects;

@ConfigurationProperties(
        prefix = "org.tbk.spring.jsr354",
        ignoreUnknownFields = false
)
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
public class SpringJsr354AutoConfigProperties {

    private boolean enabled;

    private Boolean autobootstrap;

    // see javamoney.properties and pverwrite Monatary.getConfig() update!

    public boolean isAutobootstrap() {
        return Objects.requireNonNullElse(autobootstrap, true);
    }
}
