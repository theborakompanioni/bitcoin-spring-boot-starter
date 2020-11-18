package org.tbk.bitcoin.tool.fee.bitcore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.tbk.bitcoin.tool.fee.config.AbstractFeeClientAutoConfigProperties;

@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.tool.fee.bitcore",
        ignoreUnknownFields = false
)
public class BitcoreFeeClientAutoConfigProperties extends AbstractFeeClientAutoConfigProperties {

    @Override
    protected String getDefaultBaseUrl() {
        return "https://api.bitcore.io";
    }
}
