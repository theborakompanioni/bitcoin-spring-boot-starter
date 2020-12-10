package org.tbk.bitcoin.tool.fee.earndotcom.config;

import com.google.common.cache.CacheBuilderSpec;
import com.google.common.collect.ImmutableMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.tool.fee.earndotcom.client.CachingEarndotcomApiClient;
import org.tbk.bitcoin.tool.fee.earndotcom.client.EarndotcomApiClient;
import org.tbk.bitcoin.tool.fee.earndotcom.client.EarndotcomApiClientImpl;
import org.tbk.bitcoin.tool.fee.earndotcom.provider.EarndotcomFeeProvider;
import org.tbk.bitcoin.tool.fee.earndotcom.provider.FeeSelectionStrategy;
import org.tbk.bitcoin.tool.fee.earndotcom.provider.SimpleFeeSelectionStrategy;
import org.tbk.bitcoin.tool.fee.util.MoreCacheBuilder;

import static java.util.Objects.requireNonNull;

@Configuration
@EnableConfigurationProperties(EarndotcomFeeClientAutoConfigProperties.class)
@ConditionalOnClass({
        EarndotcomApiClient.class,
        EarndotcomFeeProvider.class,
        FeeSelectionStrategy.class
})
@ConditionalOnProperty(name = {
        "org.tbk.bitcoin.tool.fee.enabled",
        "org.tbk.bitcoin.tool.fee.earndotcom.enabled"
}, havingValue = "true", matchIfMissing = true)
public class EarndotcomFeeClientAutoConfiguration {

    private final EarndotcomFeeClientAutoConfigProperties properties;

    public EarndotcomFeeClientAutoConfiguration(EarndotcomFeeClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
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
    @ConditionalOnMissingBean(FeeSelectionStrategy.class)
    public FeeSelectionStrategy earndotcomFeeSelectionStrategy() {
        return new SimpleFeeSelectionStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(EarndotcomFeeProvider.class)
    public EarndotcomFeeProvider earndotcomFeeProvider(EarndotcomApiClient earndotcomApiClient,
                                                       FeeSelectionStrategy earndotcomFeeSelectionStrategy) {
        return new EarndotcomFeeProvider(earndotcomApiClient, earndotcomFeeSelectionStrategy);
    }

    private CacheBuilderSpec defaultFeesListCacheBuilderSpec() {
        return MoreCacheBuilder.toCacheBuilderSpec(ImmutableMap.<String, String>builder()
                .put("initialCapacity", Long.toString(1))
                .put("maximumSize", Long.toString(1))
                .put("expireAfterWrite", "30s")
                .build());
    }

    private CacheBuilderSpec defaultFeesRecommendedCacheBuilderSpec() {
        return MoreCacheBuilder.toCacheBuilderSpec(ImmutableMap.<String, String>builder()
                .put("initialCapacity", Long.toString(1))
                .put("maximumSize", Long.toString(1))
                .put("expireAfterWrite", "30s")
                .build());
    }
}
