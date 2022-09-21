package org.tbk.bitcoin.jsonrpc.config;

import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.consensusj.bitcoin.jsonrpc.RpcConfig;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.jsonrpc.BitcoinJsonRpcClientFactory;
import org.tbk.bitcoin.jsonrpc.BitcoinJsonRpcClientFactoryImpl;

import static java.util.Objects.requireNonNull;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BitcoinJsonRpcClientAutoConfigProperties.class)
@ConditionalOnClass(BitcoinJsonRpcClientFactory.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.jsonrpc.enabled", havingValue = "true", matchIfMissing = true)
public class BitcoinJsonRpcClientAutoConfiguration {

    private final BitcoinJsonRpcClientAutoConfigProperties properties;

    public BitcoinJsonRpcClientAutoConfiguration(BitcoinJsonRpcClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public BitcoinJsonRpcClientFactory bitcoinJsonRpcClientFactory() {
        return new BitcoinJsonRpcClientFactoryImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public NetworkParameters bitcoinJsonRpcNetworkParameters() {
        switch (properties.getNetwork()) {
            case mainnet:
                return MainNetParams.get();
            case testnet:
                return TestNet3Params.get();
            case regtest:
                return RegTestParams.get();
            default:
                throw new IllegalArgumentException();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty({
            "org.tbk.bitcoin.jsonrpc.rpchost",
            "org.tbk.bitcoin.jsonrpc.rpcport"
    })
    public RpcConfig bitcoinJsonRpcConfig(NetworkParameters bitcoinNetworkParameters,
                                          ObjectProvider<RpcConfigBuilderCustomizer> rpcConfigBuilderCustomizer) {
        RpcConfigBuilder rpcConfigBuilder = new RpcConfigBuilder(bitcoinNetworkParameters, properties.getRpchost(), properties.getRpcport())
                .username(properties.getRpcuser())
                .password(properties.getRpcpassword());

        rpcConfigBuilderCustomizer.orderedStream().forEach(customizer -> customizer.customize(rpcConfigBuilder));

        return rpcConfigBuilder.build();
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(RpcConfig.class)
    public BitcoinClient bitcoinJsonRpcClient(BitcoinJsonRpcClientFactory bitcoinClientFactory, RpcConfig rpcConfig) {
        return bitcoinClientFactory.create(rpcConfig);
    }
}
