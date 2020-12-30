package org.tbk.spring.jsr354.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(
        prefix = "org.tbk.spring.jsr354",
        ignoreUnknownFields = false
)
public class SpringJsr354AutoConfigProperties {

    private boolean enabled;

    private boolean autobootstrap = true;

    // see javamoney.properties and pverwrite Monatary.getConfig() update!
}
