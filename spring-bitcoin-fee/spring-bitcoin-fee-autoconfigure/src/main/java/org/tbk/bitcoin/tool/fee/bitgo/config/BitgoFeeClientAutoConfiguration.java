package org.tbk.bitcoin.tool.fee.bitgo.config;

import com.google.common.cache.CacheBuilderSpec;
import com.google.common.collect.ImmutableMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.tool.fee.bitgo.BitgoFeeApiClient;
import org.tbk.bitcoin.tool.fee.bitgo.BitgoFeeApiClientImpl;
import org.tbk.bitcoin.tool.fee.bitgo.BitgoFeeProvider;
import org.tbk.bitcoin.tool.fee.bitgo.CachingBitgoFeeApiClient;
import org.tbk.bitcoin.tool.fee.util.MoreCacheBuilder;

import static java.util.Objects.requireNonNull;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BitgoFeeClientAutoConfigProperties.class)
@ConditionalOnClass({
        BitgoFeeApiClient.class,
        BitgoFeeProvider.class
})
@ConditionalOnProperty(name = {
        "org.tbk.bitcoin.tool.fee.enabled",
        "org.tbk.bitcoin.tool.fee.bitgo.enabled"
}, havingValue = "true", matchIfMissing = true)
public class BitgoFeeClientAutoConfiguration {

    private final BitgoFeeClientAutoConfigProperties properties;

    public BitgoFeeClientAutoConfiguration(BitgoFeeClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean(BitgoFeeApiClient.class)
    public BitgoFeeApiClient bitgoFeeApiClient() {
        BitgoFeeApiClientImpl bitgoFeeApiClient = new BitgoFeeApiClientImpl(properties.getBaseUrl(), properties.getToken().orElse(null));

        return CachingBitgoFeeApiClient.builder()
                .delegate(bitgoFeeApiClient)
                .responseCacheBuilderSpec(defaultResponseCacheBuilderSpec())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(BitgoFeeProvider.class)
    public BitgoFeeProvider bitgoFeeProvider(BitgoFeeApiClient bitgoFeeApiClient) {
        return new BitgoFeeProvider(bitgoFeeApiClient);
    }

    private CacheBuilderSpec defaultResponseCacheBuilderSpec() {
        return MoreCacheBuilder.toCacheBuilderSpec(ImmutableMap.<String, String>builder()
                .put("initialCapacity", Long.toString(1))
                .put("maximumSize", Long.toString(1))
                .put("expireAfterWrite", "30s")
                .build());
    }
}
