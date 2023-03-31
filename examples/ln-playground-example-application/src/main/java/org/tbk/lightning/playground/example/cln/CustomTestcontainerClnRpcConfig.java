package org.tbk.lightning.playground.example.cln;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.lightning.cln.grpc.ClnRpcConfig;
import org.tbk.lightning.cln.grpc.ClnRpcConfigImpl;
import org.tbk.lightning.cln.grpc.config.ClnClientAutoConfigProperties;
import org.tbk.spring.testcontainer.cln.ClnContainer;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class CustomTestcontainerClnRpcConfig {

    @Bean("clnRpcSslContext")
    public SslContext clnRpcSslContext(ClnClientAutoConfigProperties properties, ClnContainer<?> clnContainer) {
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

    @Bean
    public ClnRpcConfig clnRpcConfig(ClnClientAutoConfigProperties properties,
                                     ClnContainer<?> clnContainer,
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
