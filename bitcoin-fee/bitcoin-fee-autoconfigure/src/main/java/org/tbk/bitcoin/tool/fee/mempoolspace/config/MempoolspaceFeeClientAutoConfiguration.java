package org.tbk.bitcoin.tool.fee.mempoolspace.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.tbk.bitcoin.tool.fee.mempoolspace.MempoolspaceFeeApiClient;
import org.tbk.bitcoin.tool.fee.mempoolspace.MempoolspaceFeeApiClientImpl;
import org.tbk.bitcoin.tool.fee.mempoolspace.ProjectedBlocksMempoolspaceFeeProvider;
import org.tbk.bitcoin.tool.fee.mempoolspace.ProjectedBlocksMempoolspaceFeeProvider.FeesFromProjectedBlockStrategy;
import org.tbk.bitcoin.tool.fee.mempoolspace.SimpleMempoolspaceFeeProvider;

import static java.util.Objects.requireNonNull;

@AutoConfiguration
@EnableConfigurationProperties(MempoolspaceFeeClientAutoConfigProperties.class)
@ConditionalOnClass({
        MempoolspaceFeeApiClient.class
})
@ConditionalOnProperty(name = {
        "org.tbk.bitcoin.tool.fee.enabled",
        "org.tbk.bitcoin.tool.fee.mempoolspace.enabled"
}, havingValue = "true", matchIfMissing = true)
public class MempoolspaceFeeClientAutoConfiguration {

    private final MempoolspaceFeeClientAutoConfigProperties properties;

    public MempoolspaceFeeClientAutoConfiguration(MempoolspaceFeeClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean(MempoolspaceFeeApiClient.class)
    MempoolspaceFeeApiClient mempoolspaceFeeApiClient() {
        return new MempoolspaceFeeApiClientImpl(properties.getBaseUrl(), properties.getToken().orElse(null));
    }

    @Bean
    @ConditionalOnMissingBean(SimpleMempoolspaceFeeProvider.class)
    SimpleMempoolspaceFeeProvider simpleMempoolspaceFeeProvider(MempoolspaceFeeApiClient mempoolspaceFeeApiClient) {
        return new SimpleMempoolspaceFeeProvider(mempoolspaceFeeApiClient);
    }

    @Bean("projectedBlocksMempoolspaceFeeProvider")
    @ConditionalOnBean(FeesFromProjectedBlockStrategy.class)
    @ConditionalOnMissingBean(ProjectedBlocksMempoolspaceFeeProvider.class)
    ProjectedBlocksMempoolspaceFeeProvider projectedBlocksMempoolspaceFeeProviderWithCustomStrategy(MempoolspaceFeeApiClient mempoolspaceFeeApiClient,
                                                                                                    FeesFromProjectedBlockStrategy strategy) {
        return new ProjectedBlocksMempoolspaceFeeProvider(mempoolspaceFeeApiClient, strategy);
    }

    @Bean("projectedBlocksMempoolspaceFeeProvider")
    @ConditionalOnMissingBean({
            ProjectedBlocksMempoolspaceFeeProvider.class,
            FeesFromProjectedBlockStrategy.class
    })
    ProjectedBlocksMempoolspaceFeeProvider projectedBlocksMempoolspaceFeeProvider(MempoolspaceFeeApiClient mempoolspaceFeeApiClient) {
        return new ProjectedBlocksMempoolspaceFeeProvider(mempoolspaceFeeApiClient);
    }
}
