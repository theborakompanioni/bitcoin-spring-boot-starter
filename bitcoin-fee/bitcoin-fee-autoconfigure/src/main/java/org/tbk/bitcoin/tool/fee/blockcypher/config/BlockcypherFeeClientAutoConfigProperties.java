package org.tbk.bitcoin.tool.fee.blockcypher.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.tbk.bitcoin.tool.fee.config.AbstractFeeClientAutoConfigProperties;

@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.tool.fee.blockcypher",
        ignoreUnknownFields = false
)
public class BlockcypherFeeClientAutoConfigProperties extends AbstractFeeClientAutoConfigProperties {

    public BlockcypherFeeClientAutoConfigProperties(boolean enabled, String baseUrl, String token) {
        super(enabled, baseUrl, token);
    }

    @Override
    protected String getDefaultBaseUrl() {
        return "https://api.blockcypher.com";
    }
}
