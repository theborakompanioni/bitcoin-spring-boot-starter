package org.tbk.bitcoin.exchange.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.exchange",
        ignoreUnknownFields = false
)
public class BitcoinExchangeAutoConfigProperties {
    /**
     * Whether the service should be enabled
     */
    private boolean enabled;
}
