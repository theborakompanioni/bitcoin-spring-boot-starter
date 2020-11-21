package org.tbk.bitcoin.tool.fee.blockchair.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.tbk.bitcoin.tool.fee.config.AbstractFeeClientAutoConfigProperties;

@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.tool.fee.blockchair",
        ignoreUnknownFields = false
)
public class BlockchairFeeClientAutoConfigProperties extends AbstractFeeClientAutoConfigProperties {

    @Override
    protected String getDefaultBaseUrl() {
        return "https://api.blockchair.com";
    }
}
