package org.tbk.bitcoin.tool.fee.blockstreaminfo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.tbk.bitcoin.tool.fee.config.AbstractFeeClientAutoConfigProperties;

@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.tool.fee.blockstreaminfo",
        ignoreUnknownFields = false
)
public class BlockstreamInfoFeeClientAutoConfigProperties extends AbstractFeeClientAutoConfigProperties {

    public BlockstreamInfoFeeClientAutoConfigProperties(boolean enabled, String baseUrl, String token) {
        super(enabled, baseUrl, token);
    }

    @Override
    protected String getDefaultBaseUrl() {
        return "https://blockstream.info";
    }
}
