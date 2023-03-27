package org.tbk.spring.testcontainer.cln.example;

import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.lightning.cln.grpc.ClnRpcConfig;
import org.tbk.lightning.cln.grpc.ClnRpcConfigImpl;
import org.tbk.lightning.cln.grpc.config.ClnClientAutoConfigProperties;
import org.tbk.spring.testcontainer.cln.ClnContainer;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class CustomTestcontainerClnRpcConfig {

    @Bean
    public ClnRpcConfig clnRpcConfig(ClnClientAutoConfigProperties properties, ClnContainer<?> clnContainer) {
        String host = clnContainer.getHost();
        Integer mappedPort = clnContainer.getMappedPort(properties.getPort());

        return ClnRpcConfigImpl.builder()
                .host(host)
                .port(mappedPort)
                .build();
    }

    @Bean
    public SslContext clnRpcSslContext(ClnClientAutoConfigProperties properties, ClnContainer<?> clnContainer) {
        return clnContainer.copyFileFromContainer("/root/.lightning/regtest/client.pem", certStream -> {
            return clnContainer.copyFileFromContainer("/root/.lightning/regtest/client-key.pem", keyStream -> {
                return clnContainer.copyFileFromContainer("/root/.lightning/regtest/ca.pem", caStream -> {
                    return GrpcSslContexts.configure(SslContextBuilder.forClient(), SslProvider.OPENSSL).keyManager(certStream, keyStream).trustManager(caStream).build();
                });
            });
        });
    }

    @Bean(name = "clnChannelBuilder")
    public ManagedChannelBuilder<?> clnChannelBuilder(ClnRpcConfig rpcConfig, SslContext clnRpcSslContext) {
        return NettyChannelBuilder.forAddress(rpcConfig.getHost(), rpcConfig.getPort())
                .sslContext(clnRpcSslContext);
    }

}
