package org.tbk.bitcoin.jsonrpc.config;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.json.pojo.NetworkInfo;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.consensusj.jsonrpc.JsonRpcStatusException;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthContributorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.info.ConditionalOnEnabledInfoContributor;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.tbk.bitcoin.jsonrpc.actuator.health.BitcoinJsonRpcHealthIndicator;

import java.util.Map;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Objects.requireNonNull;

@AutoConfiguration
@ConditionalOnProperty(value = "org.tbk.bitcoin.jsonrpc.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({
        HealthContributor.class,
        BitcoinClient.class
})
@AutoConfigureAfter({
        BitcoinJsonRpcClientAutoConfiguration.class,
        BitcoinJsonRpcCacheAutoConfiguration.class
})
public class BitcoinJsonRpcHealthContributorAutoConfiguration {

    @AutoConfiguration
    @ConditionalOnEnabledHealthIndicator("bitcoinJsonRpc")
    @ConditionalOnBean(BitcoinClient.class)
    @AutoConfigureAfter({
            BitcoinJsonRpcClientAutoConfiguration.class,
            BitcoinJsonRpcCacheAutoConfiguration.class
    })
    // cannot be an inner static class - would not be picked up by spring correctly
    public class BitcoinJsonRpcClientHealthContributorAutoConfiguration extends
            CompositeHealthContributorConfiguration<BitcoinJsonRpcHealthIndicator, BitcoinClient> {

        public BitcoinJsonRpcClientHealthContributorAutoConfiguration() {
            super(BitcoinJsonRpcHealthIndicator::new);
        }

        @Override
        protected BitcoinJsonRpcHealthIndicator createIndicator(BitcoinClient bean) {
            return new BitcoinJsonRpcHealthIndicator(bean);
        }

        @Bean
        @ConditionalOnMissingBean(name = {"hiddenServiceHealthIndicator", "hiddenServiceHealthContributor"})
        HealthContributor bitcoinJsonRpcHealthContributor(Map<String, BitcoinClient> beans) {
            return createContributor(beans);
        }
    }

    @Bean
    @ConditionalOnSingleCandidate(BitcoinClient.class)
    @ConditionalOnEnabledInfoContributor("bitcoinJsonRpc")
    @ConditionalOnMissingBean(name = "bitcoinJsonRpcInfoContributor")
    InfoContributor bitcoinJsonRpcInfoContributor(BitcoinClient client) {
        return new BitcoinJsonRpcInfoContributor(client);
    }

    @Slf4j
    @SuppressFBWarnings(value = "SECCRLFLOG", justification = "reasonable usage")
    public static class BitcoinJsonRpcInfoContributor implements InfoContributor {
        private final BitcoinClient client;

        @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "class from external dependency")
        public BitcoinJsonRpcInfoContributor(BitcoinClient client) {
            this.client = requireNonNull(client);
        }

        @Override
        public void contribute(Info.Builder builder) {
            ImmutableMap.Builder<String, Object> detailBuilder = ImmutableMap.<String, Object>builder()
                    .put("network", firstNonNull(client.getNetParams().getId(), "<empty>"))
                    .put("server", client.getServerURI());

            try {
                NetworkInfo networkInfo = client.getNetworkInfo();
                BlockChainInfo blockChainInfo = client.getBlockChainInfo();

                builder.withDetail("bitcoinJsonRpc", detailBuilder
                        .put("bestblockhash", blockChainInfo.getBestBlockHash().toString())
                        .put("blockchaininfo", blockChainInfo)
                        .put("networkinfo", networkInfo)
                        .build());
            } catch (JsonRpcStatusException e) {
                log.warn("Exception while fetching info from {}: {}",
                        client.getServerURI(), e.getMessage());

                builder.withDetail("bitcoinJsonRpc", detailBuilder
                        .put("message", e.getMessage())
                        .put("httpMessage", firstNonNull(e.httpMessage, "<empty>"))
                        .put("httpCode", e.httpCode)
                        .put("jsonRpcCode", e.jsonRpcCode)
                        .put("response", firstNonNull(e.response, "<empty>"))
                        .build());
            } catch (Exception e) {
                log.warn("Exception while fetching info from {}: {}",
                        client.getServerURI(), e.getMessage());

                builder.withDetail("bitcoinJsonRpc", detailBuilder
                        .put("message", e.getMessage())
                        .build());
            }
        }
    }
}