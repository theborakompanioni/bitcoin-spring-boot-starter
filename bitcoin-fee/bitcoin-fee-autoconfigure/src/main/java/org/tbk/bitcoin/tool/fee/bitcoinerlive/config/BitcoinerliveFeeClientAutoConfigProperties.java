package org.tbk.bitcoin.tool.fee.bitcoinerlive.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.tbk.bitcoin.tool.fee.config.AbstractFeeClientAutoConfigProperties;

@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.tool.fee.bitcoinerlive",
        ignoreUnknownFields = false
)
public class BitcoinerliveFeeClientAutoConfigProperties extends AbstractFeeClientAutoConfigProperties {

    @Override
    protected String getDefaultBaseUrl() {
        return "https://bitcoiner.live";
    }
}
