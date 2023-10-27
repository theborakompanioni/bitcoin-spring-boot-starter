package org.tbk.bitcoin.tool.fee.bitcore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.tbk.bitcoin.tool.fee.config.AbstractFeeClientAutoConfigProperties;

@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.tool.fee.bitcore",
        ignoreUnknownFields = false
)
public class BitcoreFeeClientAutoConfigProperties extends AbstractFeeClientAutoConfigProperties {

    public BitcoreFeeClientAutoConfigProperties(boolean enabled, String baseUrl, String token) {
        super(enabled, baseUrl, token);
    }

    @Override
    protected String getDefaultBaseUrl() {
        return "https://api.bitcore.io";
    }
}
