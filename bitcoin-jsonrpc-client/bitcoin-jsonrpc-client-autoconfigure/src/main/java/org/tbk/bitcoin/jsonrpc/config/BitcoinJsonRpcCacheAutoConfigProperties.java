package org.tbk.bitcoin.jsonrpc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Data
@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.jsonrpc.cache",
        ignoreUnknownFields = false
)
public class BitcoinJsonRpcCacheAutoConfigProperties implements Validator {

    private boolean enabled;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == BitcoinJsonRpcCacheAutoConfigProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        BitcoinJsonRpcCacheAutoConfigProperties properties = (BitcoinJsonRpcCacheAutoConfigProperties) target;

    }
}
