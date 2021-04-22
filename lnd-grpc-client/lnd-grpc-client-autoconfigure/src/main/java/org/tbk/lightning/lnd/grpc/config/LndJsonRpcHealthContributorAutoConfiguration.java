package org.tbk.lightning.lnd.grpc.config;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.GetInfoResponse;
import org.lightningj.lnd.wrapper.message.NetworkInfo;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthContributorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.info.ConditionalOnEnabledInfoContributor;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.lightning.lnd.grpc.LndJsonRpcClientFactory;
import org.tbk.lightning.lnd.grpc.actuator.health.LndJsonRpcHealthIndicator;

import java.util.Map;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "org.tbk.lightning.lnd.grpc.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({
        HealthContributor.class,
        LndJsonRpcClientFactory.class
})
@AutoConfigureAfter(LndJsonRpcClientAutoConfiguration.class)
public class LndJsonRpcHealthContributorAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnEnabledHealthIndicator("lndJsonRpc")
    @ConditionalOnBean(SynchronousLndAPI.class)
    @AutoConfigureAfter(LndJsonRpcClientAutoConfiguration.class)
    public class LndJsonRpcClientHealthContributorAutoConfiguration extends
            CompositeHealthContributorConfiguration<LndJsonRpcHealthIndicator, SynchronousLndAPI> {

        @Override
        protected LndJsonRpcHealthIndicator createIndicator(SynchronousLndAPI bean) {
            return new LndJsonRpcHealthIndicator(bean);
        }

        @Bean
        @ConditionalOnMissingBean(name = {"lndJsonRpcHealthIndicator", "lndJsonRpcHealthContributor"})
        public HealthContributor lndJsonRpcHealthContributor(Map<String, SynchronousLndAPI> beans) {
            return createContributor(beans);
        }
    }

    @Bean
    @ConditionalOnSingleCandidate(SynchronousLndAPI.class)
    @ConditionalOnEnabledInfoContributor("lndJsonRpc")
    @ConditionalOnMissingBean(name = "lndJsonRpcInfoContributor")
    public InfoContributor lndJsonRpcInfoContributor(SynchronousLndAPI client) {
        return builder -> {
            ImmutableMap.Builder<String, Object> detailBuilder = ImmutableMap.<String, Object>builder()
                    .put("performValidation", client.isPerformValidation());

            try {
                GetInfoResponse info = client.getInfo();
                NetworkInfo networkInfo = client.getNetworkInfo();

                builder.withDetail("lndJsonRpc", detailBuilder
                        .put("info", LndJsonRpcHealthIndicator.createMapFromInfoResponse(info))
                        .put("networkInfo", ImmutableMap.<String, Object>builder()
                                .put("graphDiameter", networkInfo.getGraphDiameter())
                                .put("avgOutDegree", networkInfo.getAvgOutDegree())
                                .put("maxOutDegree", networkInfo.getMaxOutDegree())
                                .put("numNodes", networkInfo.getNumNodes())
                                .put("numChannels", networkInfo.getNumChannels())
                                .put("totalNetworkCapacity", networkInfo.getTotalNetworkCapacity())
                                .put("avgChannelSize", networkInfo.getAvgChannelSize())
                                .put("minChannelSize", networkInfo.getMinChannelSize())
                                .put("maxChannelSize", networkInfo.getMaxChannelSize())
                                .put("medianChannelSizeSat", networkInfo.getMedianChannelSizeSat())
                                .put("numZombieChans", networkInfo.getNumZombieChans())
                                .build())
                        .build());
            } catch (ValidationException e) {
                builder.withDetail("lndJsonRpc", detailBuilder
                        .put("validationResult", e.getValidationResult())
                        .build());
            } catch (StatusException e) {
                builder.withDetail("lndJsonRpc", detailBuilder
                        .put("status", e.getStatus())
                        .build());
            } catch (Exception e) {
                builder.withDetail("lndJsonRpc", detailBuilder
                        .put("error", e.getMessage())
                        .build());
            }
        };
    }

}