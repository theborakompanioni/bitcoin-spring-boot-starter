package org.tbk.bitcoin.jsonrpc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.jsonrpc.cache",
        ignoreUnknownFields = false
)
public class BitcoinJsonRpcCacheAutoConfigProperties {

    private boolean enabled;

}
