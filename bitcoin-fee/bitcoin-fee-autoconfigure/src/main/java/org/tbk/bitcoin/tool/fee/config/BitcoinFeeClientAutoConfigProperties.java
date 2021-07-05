package org.tbk.bitcoin.tool.fee.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.tool.fee",
        // do not throw exceptions on unknown keys:
        // this enables sub-keys for every client, e.g. "org.tbk.bitcoin.tool.fee.mempoolspace.enabled"
        ignoreUnknownFields = true
)
public class BitcoinFeeClientAutoConfigProperties {

    private boolean enabled;

}
