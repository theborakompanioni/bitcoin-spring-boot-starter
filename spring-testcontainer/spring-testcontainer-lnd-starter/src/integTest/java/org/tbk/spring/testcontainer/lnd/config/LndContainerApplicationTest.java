package org.tbk.spring.testcontainer.lnd.config;

import com.google.common.io.BaseEncoding;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import io.grpc.stub.StreamObserver;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Test;
import org.lightningj.lnd.wrapper.*;
import org.lightningj.lnd.wrapper.message.Chain;
import org.lightningj.lnd.wrapper.message.GetInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.lightning.lnd.grpc.LndRpcConfig;
import org.tbk.lightning.lnd.grpc.LndRpcConfigImpl;
import org.tbk.lightning.lnd.grpc.config.LndClientAutoConfigProperties;
import org.tbk.spring.testcontainer.lnd.LndContainer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class LndContainerApplicationTest {

    @SpringBootApplication
    public static class LndContainerTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(LndContainerTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }

        @Configuration(proxyBeanMethods = false)
        public static class CustomTestcontainerLndRpcConfiguration {

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
                    //String hex = DatatypeConverter.printHexBinary(bytes);
                    String hex = BaseEncoding.base16().lowerCase().encode(bytes);
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
    }

    @Autowired(required = false)
    private LndContainer<?> lndContainer;

    @Autowired(required = false)
    private SynchronousLndAPI lndSyncApi;

    @Autowired(required = false)
    private AsynchronousLndAPI lndAsyncApi;

    @Test
    void contextLoads() {
        assertThat(lndContainer, is(notNullValue()));
        assertThat(lndContainer.isRunning(), is(true));

        assertThat(lndSyncApi, is(notNullValue()));

        assertThat(lndAsyncApi, is(notNullValue()));
    }

    @Test
    void itShouldBeCompatibleWithLightningJ() throws StatusException, ValidationException {
        GetInfoResponse info = lndSyncApi.getInfo();
        assertThat(info, is(notNullValue()));
        assertThat(info.getVersion(), startsWith("0.15.3-beta"));
        assertThat(info.getAlias(), is("tbk-lnd-starter-test"));

        assertThat(info.getChains(), hasSize(1));
        Chain chain = info.getChains().stream().findFirst().orElseThrow();
        assertThat(chain.getNetwork(), is("regtest"));

        // TODO: wait till https://github.com/lightningj-org/lightningj/issues/79 is resolved
        //NetworkInfo networkInfo = lndSyncApi.getNetworkInfo();
        //assertThat(networkInfo, is(notNullValue()));
        //assertThat("node is running alone in the network", networkInfo.getNumNodes(), is(1));
    }

    @Test
    void itShouldBeCompatibleWithLightningJAsync() {
        Flux<GetInfoResponse> infoResponseFlux = Flux.create(emitter -> {
            try {
                lndAsyncApi.getInfo(new EmittingStreamObserver<>(emitter));
            } catch (StatusException | ValidationException e) {
                emitter.error(e);
            }
        });

        GetInfoResponse info = infoResponseFlux.blockFirst(Duration.ofSeconds(15));

        assertThat(info, is(notNullValue()));
        assertThat(info.getVersion(), startsWith("0.15.3-beta"));
        assertThat(info.getAlias(), is("tbk-lnd-starter-test"));
    }

    @RequiredArgsConstructor
    static class EmittingStreamObserver<T> implements StreamObserver<T> {

        @NonNull
        private final FluxSink<T> emitter;

        @Override
        public void onNext(T value) {
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
    }
}

