package org.tbk.bitcoin.jsonrpc.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.jsonrpc.cache",
        ignoreUnknownFields = false
)
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
public class BitcoinJsonRpcCacheAutoConfigProperties {

    private boolean enabled;

}
