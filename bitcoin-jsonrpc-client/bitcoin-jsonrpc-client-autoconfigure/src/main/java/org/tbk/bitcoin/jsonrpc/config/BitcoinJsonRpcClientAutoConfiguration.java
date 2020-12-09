package org.tbk.bitcoin.jsonrpc.config;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import com.msgilligan.bitcoinj.rpc.RpcConfig;
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

@Configuration
@EnableConfigurationProperties(BitcoinJsonRpcClientAutoConfigProperties.class)
@ConditionalOnClass(BitcoinJsonRpcClientFactory.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.jsonrpc.enabled", havingValue = "true", matchIfMissing = true)
public class BitcoinJsonRpcClientAutoConfiguration {

    private BitcoinJsonRpcClientAutoConfigProperties properties;

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
        }
        throw new IllegalArgumentException();
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

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RpcConfig.class)
    public BitcoinClient bitcoinJsonRpcClient(BitcoinJsonRpcClientFactory bitcoinClientFactory, RpcConfig rpcConfig) {
        return bitcoinClientFactory.create(rpcConfig);
    }
}
