package org.tbk.lightning.playground.example.lnd;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import lombok.extern.slf4j.Slf4j;
import org.lightningj.lnd.wrapper.MacaroonContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.lightning.lnd.grpc.LndRpcConfig;
import org.tbk.lightning.lnd.grpc.LndRpcConfigImpl;
import org.tbk.lightning.lnd.grpc.config.LndClientAutoConfigProperties;
import org.tbk.spring.testcontainer.lnd.LndContainer;

import java.util.HexFormat;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class CustomTestcontainerLndRpcConfig {

    @Bean
    public LndRpcConfig lndRpcConfig(LndClientAutoConfigProperties properties,
                                     LndContainer<?> lndContainer,
                                     MacaroonContext lndRpcMacaroonContext,
                                     SslContext lndRpcSslContext) {
        String host = lndContainer.getHost();
        Integer mappedPort = lndContainer.getMappedPort(properties.getPort());

        return LndRpcConfigImpl.builder()
                .host(host)
                .port(mappedPort)
                .macaroonContext(lndRpcMacaroonContext)
                .sslContext(lndRpcSslContext)
                .build();
    }

    @Bean
    public MacaroonContext lndRpcMacaroonContext(LndClientAutoConfigProperties properties,
                                                 LndContainer<?> lndContainer) {
        return lndContainer.copyFileFromContainer(properties.getMacaroonFilePath(), inputStream -> {
            String hex = HexFormat.of().formatHex(inputStream.readAllBytes());
            return () -> hex;
        });
    }

    @Bean
    public SslContext lndRpcSslContext(LndClientAutoConfigProperties properties,
                                       LndContainer<?> lndContainer) {
        return lndContainer.copyFileFromContainer(properties.getCertFilePath(), inputStream -> {
            return GrpcSslContexts.configure(SslContextBuilder.forClient(), SslProvider.OPENSSL)
                    .trustManager(inputStream)
                    .build();
        });
    }
}
