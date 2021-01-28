package org.tbk.bitcoin.tool.fee.blockchaininfo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.tbk.bitcoin.tool.fee.config.AbstractFeeClientAutoConfigProperties;

@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.tool.fee.blockchaininfo",
        ignoreUnknownFields = false
)
public class BlockchainInfoFeeClientAutoConfigProperties extends AbstractFeeClientAutoConfigProperties {

    @Override
    protected String getDefaultBaseUrl() {
        return "https://api.blockchain.info";
    }
}
