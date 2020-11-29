package org.tbk.spring.testcontainer.lnd.example;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.lightningj.lnd.wrapper.AsynchronousLndAPI;
import org.lightningj.lnd.wrapper.MacaroonContext;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.testcontainer.lnd.LndContainer;

import javax.xml.bind.DatatypeConverter;

@Slf4j
@Configuration
public class CustomTestcontainerLightningjConfig {
    private static final String certFileInContainer = "/lnd/.lnd/tls.cert";
    private static final String macaroonFileInContainer = "/lnd/.lnd/data/chain/bitcoin/regtest/admin.macaroon";

    @Bean
    public SynchronousLndAPI lightningjSynchronousLndApi(LndContainer<?> lndContainer,
                                                         SslContext lightningjSslContext,
                                                         MacaroonContext lightningjMacaroonContext) {
        String host = lndContainer.getHost();
        Integer mappedPort = lndContainer.getMappedPort(10009);

        return new SynchronousLndAPI(
                host,
                mappedPort,
                lightningjSslContext,
                lightningjMacaroonContext);
    }

    @Bean
    public AsynchronousLndAPI lightningjAsynchronousLndApi(LndContainer<?> lndContainer,
                                                           SslContext lightningjSslContext,
                                                           MacaroonContext lightningjMacaroonContext) {
        String host = lndContainer.getHost();
        Integer mappedPort = lndContainer.getMappedPort(10009);

        return new AsynchronousLndAPI(
                host,
                mappedPort,
                lightningjSslContext,
                lightningjMacaroonContext);
    }

    @Bean
    public MacaroonContext lightningjMacaroonContext(LndContainer<?> lndContainer) {
        return lndContainer.copyFileFromContainer(macaroonFileInContainer, inputStream -> {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            String hex = DatatypeConverter.printHexBinary(bytes);
            return () -> hex;
        });
    }

    @Bean
    public SslContext lightningjSslContext(LndContainer<?> lndContainer) {
        return lndContainer.copyFileFromContainer(certFileInContainer, inputStream -> {
            return GrpcSslContexts.configure(SslContextBuilder.forClient(), SslProvider.OPENSSL)
                    .trustManager(inputStream)
                    .build();
        });
    }
}
