package org.tbk.bitcoin.tool.fee.strike.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.tbk.bitcoin.tool.fee.config.AbstractFeeClientAutoConfigProperties;

@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.tool.fee.strike",
        ignoreUnknownFields = false
)
public class StrikeFeeClientAutoConfigProperties extends AbstractFeeClientAutoConfigProperties {

    public StrikeFeeClientAutoConfigProperties(boolean enabled, String baseUrl, String token) {
        super(enabled, baseUrl, token);
    }

    @Override
    protected String getDefaultBaseUrl() {
        return "https://bitcoinchainfees.strike.me";
    }
}
