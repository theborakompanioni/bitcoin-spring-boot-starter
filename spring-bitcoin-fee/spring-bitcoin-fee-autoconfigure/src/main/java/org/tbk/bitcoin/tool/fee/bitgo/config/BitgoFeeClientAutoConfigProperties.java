package org.tbk.bitcoin.tool.fee.bitgo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.tbk.bitcoin.tool.fee.config.AbstractFeeClientAutoConfigProperties;

@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.tool.fee.bitgo",
        ignoreUnknownFields = false
)
public class BitgoFeeClientAutoConfigProperties extends AbstractFeeClientAutoConfigProperties {

    @Override
    protected String getDefaultBaseUrl() {
        return "https://www.bitgo.com";
    }
}
