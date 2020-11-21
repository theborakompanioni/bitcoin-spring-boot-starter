package org.tbk.bitcoin.tool.fee.earndotcom.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.tbk.bitcoin.tool.fee.config.AbstractFeeClientAutoConfigProperties;

@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.tool.fee.earndotcom",
        ignoreUnknownFields = false
)
public class EarndotcomFeeClientAutoConfigProperties extends AbstractFeeClientAutoConfigProperties {

    @Override
    protected String getDefaultBaseUrl() {
        return "https://bitcoinfees.earn.com";
    }
}
