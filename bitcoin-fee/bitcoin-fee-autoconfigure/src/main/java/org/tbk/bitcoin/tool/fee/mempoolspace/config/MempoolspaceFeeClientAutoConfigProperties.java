package org.tbk.bitcoin.tool.fee.mempoolspace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.tbk.bitcoin.tool.fee.config.AbstractFeeClientAutoConfigProperties;

@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.tool.fee.mempoolspace",
        ignoreUnknownFields = false
)
public class MempoolspaceFeeClientAutoConfigProperties extends AbstractFeeClientAutoConfigProperties {

    @Override
    protected String getDefaultBaseUrl() {
        // alternatives are:
        // - https://mempool.bisq.services
        // - https://mempool.emzy.de
        // - https://mempool.ninja
        return "https://mempool.space";
    }
}
