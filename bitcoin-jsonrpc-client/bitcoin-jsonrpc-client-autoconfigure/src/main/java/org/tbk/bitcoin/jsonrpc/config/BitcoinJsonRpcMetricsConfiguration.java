package org.tbk.bitcoin.jsonrpc.config;

import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.cache.GuavaCacheMetrics;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.jsonrpc.cache.CacheFacade;

import java.util.Collections;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "org.tbk.bitcoin.jsonrpc.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(MeterBinder.class)
@AutoConfigureAfter({
        BitcoinJsonRpcClientAutoConfiguration.class,
        BitcoinJsonRpcCacheAutoConfiguration.class
})
public class BitcoinJsonRpcMetricsConfiguration {

    @Bean
    @ConditionalOnSingleCandidate(CacheFacade.class)
    public MeterBinder bitcoinJsonRpcCacheMetrics(CacheFacade cache) {
        return (registry) -> {
            GuavaCacheMetrics.monitor(registry, cache.block(), "block", Collections.emptyList());
            GuavaCacheMetrics.monitor(registry, cache.blockInfo(), "blockInfo", Collections.emptyList());
            GuavaCacheMetrics.monitor(registry, cache.tx(), "tx", Collections.emptyList());
            GuavaCacheMetrics.monitor(registry, cache.txInfo(), "txInfo", Collections.emptyList());
        };
    }
}
