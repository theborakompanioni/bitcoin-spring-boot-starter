package org.tbk.bitcoin.regtest.config;

import org.bitcoinj.core.NetworkParameters;
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient;
import org.consensusj.bitcoin.jsonrpc.RpcConfig;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.tbk.bitcoin.jsonrpc.BitcoinJsonRpcClientFactory;
import org.tbk.bitcoin.jsonrpc.config.BitcoinJsonRpcClientAutoConfigProperties;
import org.tbk.bitcoin.jsonrpc.config.BitcoinJsonRpcClientAutoConfiguration;

import static java.util.Objects.requireNonNull;

@AutoConfiguration
@EnableConfigurationProperties(BitcoinRegtestAutoConfigProperties.class)
@ConditionalOnClass(BitcoinJsonRpcClientFactory.class)
@AutoConfigureAfter(BitcoinJsonRpcClientAutoConfiguration.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.regtest.enabled", havingValue = "true")
public class BitcoinRegtestAutoConfiguration {

    private final BitcoinJsonRpcClientAutoConfigProperties properties;

    public BitcoinRegtestAutoConfiguration(BitcoinJsonRpcClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Primary
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RpcConfig.class)
    BitcoinExtendedClient bitcoinRegtestClient(RpcConfig rpcConfig) {
        String requiredNetworkId = NetworkParameters.ID_REGTEST;
        String configuredNetworkId = rpcConfig.getNetParams().getId();

        boolean isRegtest = configuredNetworkId.equals(requiredNetworkId);
        if (!isRegtest) {
            String errorMessage = String.format("Bitcoin must be configured with network '%s' - got '%s'",
                    requiredNetworkId, configuredNetworkId);
            throw new BeanCreationNotAllowedException("bitcoinRegtestClient", errorMessage);
        }

        return new BitcoinExtendedClient(rpcConfig);
    }
}
