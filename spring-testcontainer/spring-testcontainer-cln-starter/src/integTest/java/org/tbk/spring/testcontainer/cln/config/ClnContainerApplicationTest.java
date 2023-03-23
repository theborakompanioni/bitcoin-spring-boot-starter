package org.tbk.spring.testcontainer.cln.config;

import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import io.grpc.stub.StreamObserver;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.lightning.cln.grpc.ClnRpcConfig;
import org.tbk.lightning.cln.grpc.ClnRpcConfigImpl;
import org.tbk.lightning.cln.grpc.client.GetinfoRequest;
import org.tbk.lightning.cln.grpc.client.GetinfoResponse;
import org.tbk.lightning.cln.grpc.client.NodeGrpc;
import org.tbk.lightning.cln.grpc.config.ClnClientAutoConfigProperties;
import org.tbk.spring.testcontainer.cln.ClnContainer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class ClnContainerApplicationTest {

    @SpringBootApplication
    public static class ClnContainerTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(ClnContainerTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }

        @Configuration(proxyBeanMethods = false)
        public static class CustomTestcontainerClnRpcConfiguration {

            @Bean
            public ClnRpcConfig clnRpcConfig(ClnClientAutoConfigProperties properties,
                                             ClnContainer<?> clnContainer) {
                String host = clnContainer.getHost();
                Integer mappedPort = clnContainer.getMappedPort(properties.getPort());

                return ClnRpcConfigImpl.builder()
                        .host(host)
                        .port(mappedPort)
                        .build();
            }

            @Bean
            public SslContext clnRpcSslContext(ClnClientAutoConfigProperties properties,
                                               ClnContainer<?> clnContainer) {
                /*String key = clnContainer.copyFileFromContainer("/root/.lightning/regtest/client-key.pem", inputStream -> {
                    return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                });

                String cert = clnContainer.copyFileFromContainer("/root/.lightning/regtest/client.pem", inputStream -> {
                    return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                });*/
                // properties.getCertFilePath()
                return clnContainer.copyFileFromContainer("/root/.lightning/regtest/client.pem", certStream -> {
                    return clnContainer.copyFileFromContainer("/root/.lightning/regtest/client-key.pem", keyStream -> {
                        return clnContainer.copyFileFromContainer("/root/.lightning/regtest/ca.pem", caStream -> {
                            return GrpcSslContexts.configure(SslContextBuilder.forClient(), SslProvider.OPENSSL)
                                    .keyManager(certStream, keyStream)
                                    .trustManager(caStream)
                                    .build();
                        });
                    });
                });
            }

            @Bean(name = "clnChannelBuilder")
            public ManagedChannelBuilder<?> clnChannelBuilder(ClnRpcConfig rpcConfig,
                                                              SslContext clnRpcSslContext) {
                NettyChannelBuilder nettyChannelBuilder = NettyChannelBuilder
                        .forAddress(rpcConfig.getHost(), rpcConfig.getPort())
                        .sslContext(clnRpcSslContext);
                return nettyChannelBuilder;
            }

        }
    }

    @Autowired(required = false)
    private ClnContainer<?> clnContainer;

    @Autowired(required = false)
    private NodeGrpc.NodeBlockingStub clnNodeBlockingStub;

    @Autowired(required = false)
    private NodeGrpc.NodeStub clnNodeStub;

    @Autowired(required = false)
    private NodeGrpc.NodeFutureStub clnNodeFutureStub;

    @Test
    void contextLoads() {
        assertThat(clnContainer, is(notNullValue()));
        assertThat(clnContainer.isRunning(), is(true));
        assertThat(clnNodeStub, is(notNullValue()));
        assertThat(clnNodeFutureStub, is(notNullValue()));
        assertThat(clnNodeBlockingStub, is(notNullValue()));
    }

    @Test
    void itShouldSuccessfullyInvokeGetInfoBlocking() {
        GetinfoResponse response = clnNodeBlockingStub.getinfo(GetinfoRequest.newBuilder().build());

        assertThat(response, is(notNullValue()));
        assertThat(response.getAlias(), is("tbk-cln-starter-test"));
        assertThat(response.getNetwork(), is("regtest"));
    }

    @Test
    void itShouldSuccessfullyInvokeGetInfoFuture() throws ExecutionException, InterruptedException, TimeoutException {
        GetinfoResponse response = clnNodeFutureStub.getinfo(GetinfoRequest.newBuilder().build())
                .get(5, TimeUnit.SECONDS);

        assertThat(response, is(notNullValue()));
        assertThat(response.getAlias(), is("tbk-cln-starter-test"));
        assertThat(response.getNetwork(), is("regtest"));
    }

    @Test
    void itShouldSuccessfullyInvokeGetInfo() {
        GetinfoResponse response = Flux.<GetinfoResponse>create(emitter -> {
            try {
                clnNodeStub.getinfo(GetinfoRequest.newBuilder().build(), new EmittingStreamObserver<>(emitter));
            } catch (Exception e) {
                emitter.error(e);
            }
        }).blockFirst(Duration.ofSeconds(5));

        assertThat(response, is(notNullValue()));
        assertThat(response.getAlias(), is("tbk-cln-starter-test"));
        assertThat(response.getNetwork(), is("regtest"));
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
