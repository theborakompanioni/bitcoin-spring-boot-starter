package org.tbk.spring.testcontainer.cln.config.grpc;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import lombok.extern.slf4j.Slf4j;
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
import org.tbk.lightning.cln.grpc.ClnRpcConfig;
import org.tbk.lightning.cln.grpc.ClnRpcConfigImpl;
import org.tbk.lightning.cln.grpc.config.ClnClientAutoConfigProperties;
import org.tbk.lightning.cln.grpc.config.ClnClientAutoConfiguration;
import org.tbk.spring.testcontainer.cln.ClnContainer;
import org.tbk.spring.testcontainer.cln.config.ClnContainerAutoConfiguration;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({
        ClnRpcConfig.class,
        ClnClientAutoConfigProperties.class
})
@EnableConfigurationProperties(ClnClientAutoConfigProperties.class)
@AutoConfigureAfter(ClnContainerAutoConfiguration.class)
@AutoConfigureBefore(ClnClientAutoConfiguration.class)
@ConditionalOnProperty(value = "org.tbk.lightning.cln.grpc.enabled", havingValue = "true", matchIfMissing = true)
public class ClnContainerRpcClientAutoConfiguration {

    private final ClnClientAutoConfigProperties properties;

    public ClnContainerRpcClientAutoConfiguration(ClnClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean("clnRpcSslContext")
    @ConditionalOnMissingBean(name = {"clnRpcSslContext"})
    @ConditionalOnBean({ClnContainer.class})
    SslContext clnRpcSslContext(ClnContainer<?> clnContainer) {
        return clnContainer.copyFileFromContainer(properties.getClientCertFilePath(), certStream -> {
            return clnContainer.copyFileFromContainer(properties.getClientKeyFilePath(), keyStream -> {
                return clnContainer.copyFileFromContainer(properties.getCaCertFilePath(), caStream -> {
                    return GrpcSslContexts.configure(SslContextBuilder.forClient(), SslProvider.OPENSSL)
                            .keyManager(certStream, keyStream)
                            .trustManager(caStream)
                            .build();
                });
            });
        });
    }

    @Bean("clnRpcConfig")
    @ConditionalOnMissingBean(ClnRpcConfig.class)
    @ConditionalOnBean({ClnContainer.class})
    ClnRpcConfig clnRpcConfig(ClnContainer<?> clnContainer,
                                     @Qualifier("clnRpcSslContext") SslContext clnRpcSslContext) {
        String host = clnContainer.getHost();
        Integer mappedPort = clnContainer.getMappedPort(properties.getPort());

        return ClnRpcConfigImpl.builder()
                .host(host)
                .port(mappedPort)
                .sslContext(clnRpcSslContext)
                .build();
    }
}
