package org.tbk.bitcoin.tool.fee.btcdotcom.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.tbk.bitcoin.tool.fee.config.AbstractFeeClientAutoConfigProperties;

@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.tool.fee.btcdotcom",
        ignoreUnknownFields = false
)
public class BtcdotcomFeeClientAutoConfigProperties extends AbstractFeeClientAutoConfigProperties {

    public BtcdotcomFeeClientAutoConfigProperties(boolean enabled, String baseUrl, String token) {
        super(enabled, baseUrl, token);
    }

    @Override
    protected String getDefaultBaseUrl() {
        return "https://btc.com";
    }
}
