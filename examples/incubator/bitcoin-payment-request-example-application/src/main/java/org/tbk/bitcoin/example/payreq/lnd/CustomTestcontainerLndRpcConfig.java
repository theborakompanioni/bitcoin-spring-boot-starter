package org.tbk.bitcoin.example.payreq.lnd;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.lightningj.lnd.wrapper.MacaroonContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.lightning.lnd.grpc.LndRpcConfig;
import org.tbk.lightning.lnd.grpc.LndRpcConfigImpl;
import org.tbk.lightning.lnd.grpc.config.LndClientAutoConfigProperties;
import org.tbk.spring.testcontainer.lnd.LndContainer;

import javax.xml.bind.DatatypeConverter;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class CustomTestcontainerLndRpcConfig {

    @Bean
    public LndRpcConfig lndRpcConfig(LndClientAutoConfigProperties properties,
                                     LndContainer<?> lndContainer,
                                     MacaroonContext lndRpcMacaroonContext,
                                     SslContext lndRpcSslContext) {
        String host = lndContainer.getHost();
        Integer mappedPort = lndContainer.getMappedPort(properties.getRpcport());

        return LndRpcConfigImpl.builder()
                .rpchost(host)
                .rpcport(mappedPort)
                .macaroonContext(lndRpcMacaroonContext)
                .sslContext(lndRpcSslContext)
                .build();
    }

    @Bean
    public MacaroonContext lndRpcMacaroonContext(LndClientAutoConfigProperties properties,
                                                 LndContainer<?> lndContainer) {
        return lndContainer.copyFileFromContainer(properties.getMacaroonFilePath(), inputStream -> {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            String hex = DatatypeConverter.printHexBinary(bytes);
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
