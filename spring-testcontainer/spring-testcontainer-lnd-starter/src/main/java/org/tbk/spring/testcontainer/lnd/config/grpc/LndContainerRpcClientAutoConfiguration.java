package org.tbk.spring.testcontainer.lnd.config.grpc;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import lombok.extern.slf4j.Slf4j;
import org.lightningj.lnd.wrapper.MacaroonContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.lightning.lnd.grpc.LndRpcConfig;
import org.tbk.lightning.lnd.grpc.LndRpcConfigImpl;
import org.tbk.lightning.lnd.grpc.config.LndClientAutoConfigProperties;
import org.tbk.lightning.lnd.grpc.config.LndClientAutoConfiguration;
import org.tbk.spring.testcontainer.lnd.LndContainer;
import org.tbk.spring.testcontainer.lnd.config.LndContainerAutoConfiguration;

import java.util.HexFormat;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({
        LndRpcConfig.class,
        LndClientAutoConfigProperties.class
})
@EnableConfigurationProperties(LndClientAutoConfigProperties.class)
@AutoConfigureAfter(LndContainerAutoConfiguration.class)
@AutoConfigureBefore(LndClientAutoConfiguration.class)
@ConditionalOnProperty(value = "org.tbk.lightning.lnd.grpc.enabled", havingValue = "true", matchIfMissing = true)
public class LndContainerRpcClientAutoConfiguration {

    private final LndClientAutoConfigProperties properties;

    public LndContainerRpcClientAutoConfiguration(LndClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean("lndRpcMacaroonContext")
    @ConditionalOnMissingBean(name = {"lndRpcMacaroonContext"})
    @ConditionalOnBean({LndContainer.class})
    MacaroonContext lndRpcMacaroonContext(LndContainer<?> lndContainer) {
        return lndContainer.copyFileFromContainer(properties.getMacaroonFilePath(), inputStream -> {
            String hex = HexFormat.of().formatHex(inputStream.readAllBytes());
            return () -> hex;
        });
    }

    @Bean("lndRpcSslContext")
    @ConditionalOnMissingBean(name = {"lndRpcSslContext"})
    @ConditionalOnBean({LndContainer.class})
    SslContext lndRpcSslContext(LndContainer<?> lndContainer) {
        return lndContainer.copyFileFromContainer(properties.getCertFilePath(), inputStream -> {
            return GrpcSslContexts.configure(SslContextBuilder.forClient(), SslProvider.OPENSSL)
                    .trustManager(inputStream)
                    .build();
        });
    }

    @Bean("lndRpcConfig")
    @ConditionalOnMissingBean(LndRpcConfig.class)
    @ConditionalOnBean({LndContainer.class})
    LndRpcConfig lndRpcConfig(LndContainer<?> lndContainer,
                                     @Qualifier("lndRpcMacaroonContext") MacaroonContext lndRpcMacaroonContext,
                                     @Qualifier("lndRpcSslContext") SslContext lndRpcSslContext) {
        String host = lndContainer.getHost();
        Integer mappedPort = lndContainer.getMappedPort(properties.getPort());

        return LndRpcConfigImpl.builder()
                .host(host)
                .port(mappedPort)
                .macaroonContext(lndRpcMacaroonContext)
                .sslContext(lndRpcSslContext)
                .build();
    }
}
