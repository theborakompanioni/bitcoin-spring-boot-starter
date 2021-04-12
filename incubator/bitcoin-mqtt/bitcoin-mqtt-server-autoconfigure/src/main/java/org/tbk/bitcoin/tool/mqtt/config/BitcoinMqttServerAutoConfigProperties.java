package org.tbk.bitcoin.tool.mqtt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Data
@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.tool.mqtt.server",
        ignoreUnknownFields = false
)
public class BitcoinMqttServerAutoConfigProperties implements Validator {
    /**
     * Whether the autoconfig should be enabled
     */
    private boolean enabled;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == BitcoinMqttServerAutoConfigProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        BitcoinMqttServerAutoConfigProperties properties = (BitcoinMqttServerAutoConfigProperties) target;

    }
}
