package org.tbk.spring.testcontainer.lnd.config;

import io.grpc.stub.StreamObserver;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.lightningj.lnd.wrapper.AsynchronousLndAPI;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.Chain;
import org.lightningj.lnd.wrapper.message.GetInfoResponse;
import org.lightningj.lnd.wrapper.message.NetworkInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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
        assertThat(info.getVersion(), startsWith("0.16.0-beta"));
        assertThat(info.getAlias(), is("tbk-lnd-starter-test"));

        assertThat(info.getChains(), hasSize(1));
        Chain chain = info.getChains().stream().findFirst().orElseThrow();
        assertThat(chain.getNetwork(), is("regtest"));

        NetworkInfo networkInfo = lndSyncApi.getNetworkInfo();
        assertThat(networkInfo, is(notNullValue()));
        assertThat("node is running alone in the network", networkInfo.getNumNodes(), is(0));
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
        assertThat(info.getVersion(), startsWith("0.16.0-beta"));
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

