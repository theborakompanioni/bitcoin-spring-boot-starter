package org.tbk.bitcoin.tool.mqtt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.tool.mqtt.server",
        ignoreUnknownFields = false
)
public class BitcoinMqttServerAutoConfigProperties {
    /**
     * Whether the autoconfig should be enabled.
     */
    private boolean enabled;

    private String clientId = "BTC_MQTT_BROKER";
}
