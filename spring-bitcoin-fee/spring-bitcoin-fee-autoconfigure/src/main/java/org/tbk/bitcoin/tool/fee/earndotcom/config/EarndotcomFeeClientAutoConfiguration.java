package org.tbk.bitcoin.tool.fee.earndotcom.config;

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.collect.ImmutableMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.tool.fee.earndotcom.CachingEarndotcomApiClient;
import org.tbk.bitcoin.tool.fee.earndotcom.EarndotcomApiClient;
import org.tbk.bitcoin.tool.fee.earndotcom.EarndotcomApiClientImpl;
import org.tbk.bitcoin.tool.fee.earndotcom.EarndotcomFeeProvider;

import java.util.Map;

import static java.util.Objects.requireNonNull;

@Configuration
@EnableConfigurationProperties(EarndotcomFeeClientAutoConfigProperties.class)
@ConditionalOnProperty(name = {
        "org.tbk.bitcoin.tool.fee.enabled",
        "org.tbk.bitcoin.tool.fee.earndotcom.enabled"
}, havingValue = "true", matchIfMissing = true)
public class EarndotcomFeeClientAutoConfiguration {
    private static final Joiner.MapJoiner cacheBuilderSpecMapJoiner = Joiner.on(",").withKeyValueSeparator("=");

    private final EarndotcomFeeClientAutoConfigProperties properties;

    public EarndotcomFeeClientAutoConfiguration(EarndotcomFeeClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnClass(EarndotcomApiClient.class)
    @ConditionalOnMissingBean(EarndotcomApiClient.class)
    public EarndotcomApiClient earndotcomApiClient() {
        EarndotcomApiClientImpl client = new EarndotcomApiClientImpl(properties.getBaseUrl(), properties.getToken().orElse(null));

        return CachingEarndotcomApiClient.builder()
                .delegate(client)
                .feesListCacheBuilderSpec(defaultFeesListCacheBuilderSpec())
                .feesRecommendedCacheBuilderSpec(defaultFeesRecommendedCacheBuilderSpec())
                .build();
    }

    @Bean
    @ConditionalOnClass(EarndotcomFeeProvider.class)
    @ConditionalOnMissingBean(EarndotcomFeeProvider.class)
    public EarndotcomFeeProvider earndotcomFeeProvider(EarndotcomApiClient earndotcomApiClient) {
        return new EarndotcomFeeProvider(earndotcomApiClient);
    }

    private CacheBuilderSpec defaultFeesListCacheBuilderSpec() {
        return toCacheBuilderSpec(ImmutableMap.<String, String>builder()
                .put("initialCapacity", Long.toString(1))
                .put("maximumSize", Long.toString(1))
                .put("expireAfterWrite", "30s")
                .build());
    }

    private CacheBuilderSpec defaultFeesRecommendedCacheBuilderSpec() {
        return toCacheBuilderSpec(ImmutableMap.<String, String>builder()
                .put("initialCapacity", Long.toString(1))
                .put("maximumSize", Long.toString(1))
                .put("expireAfterWrite", "30s")
                .build());
    }

    private CacheBuilderSpec toCacheBuilderSpec(Map<String, String> configValues) {
        return CacheBuilderSpec.parse(cacheBuilderSpecMapJoiner.join(configValues));
    }
}
