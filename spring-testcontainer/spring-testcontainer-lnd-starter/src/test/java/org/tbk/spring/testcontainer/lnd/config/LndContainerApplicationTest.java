package org.tbk.spring.testcontainer.lnd.config;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lightningj.lnd.wrapper.*;
import org.lightningj.lnd.wrapper.message.Chain;
import org.lightningj.lnd.wrapper.message.GetInfoResponse;
import org.lightningj.lnd.wrapper.message.NetworkInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.tbk.lightning.lnd.jsonrpc.LndRpcConfig;
import org.tbk.lightning.lnd.jsonrpc.LndRpcConfigImpl;
import org.tbk.lightning.lnd.jsonrpc.config.LndJsonRpcClientAutoConfigProperties;
import org.tbk.spring.testcontainer.lnd.LndContainer;
import reactor.core.publisher.Flux;

import javax.xml.bind.DatatypeConverter;
import java.time.Duration;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class LndContainerApplicationTest {

    @SpringBootApplication
    public static class LndContainerTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(LndContainerTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }

        @Configuration
        public static class CustomTestcontainerLndJsonRpcConfiguration {

            @Bean
            public LndRpcConfig lndRpcConfig(LndJsonRpcClientAutoConfigProperties properties,
                                             LndContainer<?> lndContainer,
                                             MacaroonContext lndJsonRpcMacaroonContext,
                                             SslContext lndJsonRpcSslContext) {
                String host = lndContainer.getHost();
                Integer mappedPort = lndContainer.getMappedPort(properties.getRpcport());

                return LndRpcConfigImpl.builder()
                        .rpchost(host)
                        .rpcport(mappedPort)
                        .macaroonContext(lndJsonRpcMacaroonContext)
                        .sslContext(lndJsonRpcSslContext)
                        .build();
            }

            @Bean
            public MacaroonContext lndJsonRpcMacaroonContext(LndJsonRpcClientAutoConfigProperties properties,
                                                             LndContainer<?> lndContainer) {
                return lndContainer.copyFileFromContainer(properties.getMacaroonFilePath(), inputStream -> {
                    byte[] bytes = IOUtils.toByteArray(inputStream);
                    String hex = DatatypeConverter.printHexBinary(bytes);
                    return () -> hex;
                });
            }

            @Bean
            public SslContext lndJsonRpcSslContext(LndJsonRpcClientAutoConfigProperties properties,
                                                   LndContainer<?> lndContainer) {
                return lndContainer.copyFileFromContainer(properties.getCertFilePath(), inputStream -> {
                    return GrpcSslContexts.configure(SslContextBuilder.forClient(), SslProvider.OPENSSL)
                            .trustManager(inputStream)
                            .build();
                });
            }
        }
    }

    @Autowired(required = false)
    private LndContainer<?> lndContainer;

    @Autowired(required = false)
    private SynchronousLndAPI lndSyncApi;

    @Autowired(required = false)
    private AsynchronousLndAPI lndAsyncApi;

    @Test
    public void contextLoads() {
        assertThat(lndContainer, is(notNullValue()));
        assertThat(lndContainer.isRunning(), is(true));

        assertThat(lndSyncApi, is(notNullValue()));

        assertThat(lndAsyncApi, is(notNullValue()));
    }

    @Test
    public void itShouldBeCompatibleWithLightningJ() throws StatusException, ValidationException {
        assertThat(lndSyncApi, is(notNullValue()));

        GetInfoResponse info = lndSyncApi.getInfo();
        assertThat(info, is(notNullValue()));
        assertThat(info.getVersion(), startsWith("0.11.1-beta"));
        assertThat(info.getAlias(), is("tbk-lnd-starter-test"));

        assertThat(info.getChains(), hasSize(1));
        Chain chain = info.getChains().stream().findFirst().orElseThrow();
        assertThat(chain.getNetwork(), is("regtest"));

        NetworkInfo networkInfo = lndSyncApi.getNetworkInfo();
        assertThat(networkInfo, is(notNullValue()));
        assertThat("node is running alone in the network", networkInfo.getNumNodes(), is(1));
    }

    @Test
    public void itShouldBeCompatibleWithLightningJAsync() {
        assertThat(lndAsyncApi, is(notNullValue()));

        Flux<GetInfoResponse> infoResponseFlux = Flux.create(emitter -> {
            try {
                lndAsyncApi.getInfo(new StreamObserver<>() {
                    @Override
                    public void onNext(GetInfoResponse value) {
                        emitter.next(value);
                    }

                    @Override
                    public void onError(Throwable t) {
                        emitter.error(t);
                    }

                    @Override
                    public void onCompleted() {
                        emitter.complete();
                    }
                });
            } catch (StatusException | ValidationException e) {
                emitter.error(e);
            }
        });

        GetInfoResponse info = infoResponseFlux.blockLast(Duration.ofSeconds(10));

        assertThat(info, is(notNullValue()));
        assertThat(info.getVersion(), startsWith("0.11.1-beta"));
        assertThat(info.getAlias(), is("tbk-lnd-starter-test"));
    }
}

