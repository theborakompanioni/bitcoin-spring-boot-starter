package org.tbk.bitcoin.jsonrpc.config;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import com.msgilligan.bitcoinj.rpc.RpcConfig;
import lombok.NonNull;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.jsonrpc.BitcoinClientFactory;
import org.tbk.bitcoin.jsonrpc.BitcoinClientFactoryImpl;

import java.net.URI;

import static java.util.Objects.requireNonNull;

@Configuration
@EnableConfigurationProperties(BitcoinJsonRpcClientAutoConfigProperties.class)
@ConditionalOnClass(BitcoinClientFactory.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.jsonrpc.enabled", havingValue = "true", matchIfMissing = true)
public class BitcoinJsonRpcClientAutoConfiguration {

    private BitcoinJsonRpcClientAutoConfigProperties properties;

    public BitcoinJsonRpcClientAutoConfiguration(BitcoinJsonRpcClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public BitcoinClientFactory bitcoinClientFactory() {
        return new BitcoinClientFactoryImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public NetworkParameters bitcoinNetworkParameters() {
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
    public RpcConfig bitcoinRpcConfig(NetworkParameters bitcoinNetworkParameters) {
        URI uri = URI.create(properties.getRpchost() + ":" + properties.getRpcport());
        return new RpcConfig(bitcoinNetworkParameters, uri, properties.getRpcuser(), properties.getRpcpassword());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RpcConfig.class)
    public BitcoinClient bitcoinClient(BitcoinClientFactory bitcoinClientFactory, RpcConfig rpcConfig) {
        return bitcoinClientFactory.create(rpcConfig);
    }
}
