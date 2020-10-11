package org.tbk.bitcoin.client.config;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import com.msgilligan.bitcoinj.rpc.RpcConfig;
import lombok.NonNull;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.client.BitcoinClientFactory;
import org.tbk.bitcoin.client.BitcoinClientFactoryImpl;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(BitcoinClientAutoConfigProperties.class)
@ConditionalOnClass(BitcoinClientFactory.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.enabled", havingValue = "true")
public class BitcoinClientAutoConfiguration {

    @NonNull
    private BitcoinClientAutoConfigProperties properties;

    public BitcoinClientAutoConfiguration(BitcoinClientAutoConfigProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public BitcoinClientFactory bitcoinClientFactory() {
        return new BitcoinClientFactoryImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "org.tbk.bitcoin.client.enabled", havingValue = "true")
    public NetworkParameters bitcoinNetworkParameters() {
        return properties.getClientOrEmpty()
                .map(BitcoinClientAutoConfigProperties.BitcoinClientProperties::getNetwork)
                .map(network -> {
                    switch (network) {
                        case mainnet:
                            return MainNetParams.get();
                        case testnet:
                            return TestNet3Params.get();
                        case regtest:
                            return RegTestParams.get();
                    }
                    throw new IllegalArgumentException();
                }).orElseThrow(() -> new IllegalStateException("Cannot create bitcoinNetworkParameters"));
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "org.tbk.bitcoin.client.enabled", havingValue = "true")
    public RpcConfig bitcoinRpcConfig(NetworkParameters bitcoinNetworkParameters) {
        return properties.getClientOrEmpty()
                .map(val -> {
                    URI uri = URI.create(val.getRpchost() + ":" + val.getRpcport());
                    return new RpcConfig(bitcoinNetworkParameters, uri, val.getRpcuser(), val.getRpcpassword());
                }).orElseThrow(() -> new IllegalStateException("Cannot create bitcoinRpcConfig"));
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "org.tbk.bitcoin.client.enabled", havingValue = "true")
    public BitcoinClient bitcoinClient(BitcoinClientFactory bitcoinClientFactory, RpcConfig rpcConfig) {
        return bitcoinClientFactory.create(rpcConfig);
    }
}
