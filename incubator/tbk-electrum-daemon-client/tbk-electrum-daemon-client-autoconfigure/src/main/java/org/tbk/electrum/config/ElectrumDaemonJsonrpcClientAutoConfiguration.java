package org.tbk.electrum.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.ElectrumClientFactory;
import org.tbk.electrum.ElectrumClientFactoryImpl;

import java.net.URI;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ElectrumDaemonJsonrpcClientProperties.class)
@ConditionalOnClass(ElectrumClientFactory.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.electrum-daemon.jsonrpc.enabled", havingValue = "true")
public class ElectrumDaemonJsonrpcClientAutoConfiguration {

    private ElectrumDaemonJsonrpcClientProperties properties;

    public ElectrumDaemonJsonrpcClientAutoConfiguration(ElectrumDaemonJsonrpcClientProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ElectrumClient electrumClient(ElectrumDeamonJsonrpcConfig electrumDeamonJsonrpcConfig,
                                         ElectrumClientFactory factory) {
        URI uri = electrumDeamonJsonrpcConfig.getUri();

        return factory.create(uri, electrumDeamonJsonrpcConfig.getUsername(), electrumDeamonJsonrpcConfig.getPassword());
    }

    @Bean
    @ConditionalOnMissingBean
    public ElectrumClientFactory electrumClientFactory() {
        return new ElectrumClientFactoryImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public ElectrumDeamonJsonrpcConfig electrumDeamonJsonrpcConfig(ObjectProvider<ElectrumDeamonJsonrpcConfigBuilderCustomizer> rpcConfigBuilderCustomizer) {
        ElectrumDeamonJsonrpcConfigBuilder rpcConfigBuilder = new ElectrumDeamonJsonrpcConfigBuilder()
                .host(properties.getRpchost())
                .port(properties.getRpcport())
                .username(properties.getRpcuser())
                .password(properties.getRpcpassword());

        rpcConfigBuilderCustomizer.orderedStream().forEach(customizer -> customizer.customize(rpcConfigBuilder));

        return rpcConfigBuilder.build();
    }
}
