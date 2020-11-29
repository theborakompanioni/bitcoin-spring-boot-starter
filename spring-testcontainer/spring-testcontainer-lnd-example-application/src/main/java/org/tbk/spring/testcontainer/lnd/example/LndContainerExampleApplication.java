package org.tbk.spring.testcontainer.lnd.example;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.lightningj.lnd.proto.LightningApi;
import org.lightningj.lnd.wrapper.AsynchronousLndAPI;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.AddInvoiceResponse;
import org.lightningj.lnd.wrapper.message.ChannelEventUpdate;
import org.lightningj.lnd.wrapper.message.GetInfoResponse;
import org.lightningj.lnd.wrapper.message.Invoice;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static java.util.Objects.requireNonNull;

@Slf4j
@SpringBootApplication
public class LndContainerExampleApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(LndContainerExampleApplication.class)
                .listeners(applicationPidFileWriter())
                .web(WebApplicationType.NONE)
                .profiles("development", "local")
                .run(args);
    }

    public static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }

    private final SynchronousLndAPI lndApi;

    public LndContainerExampleApplication(SynchronousLndAPI lndApi) {
        this.lndApi = requireNonNull(lndApi);
    }

    @Bean
    public ApplicationRunner mainRunner() {
        return args -> {
            GetInfoResponse info = lndApi.getInfo();
            log.info("=================================================");
            log.info("[lnd] identity_pubkey: {}", info.getIdentityPubkey());
            log.info("[lnd] alias: {}", info.getAlias());
            log.info("[lnd] version: {}", info.getVersion());
        };
    }

    @Bean
    public ApplicationRunner lndBestBlockLogger(MessagePublishService<Block> bitcoinBlockPublishService) {
        return args -> {
            bitcoinBlockPublishService.awaitRunning(Duration.ofSeconds(20));
            Disposable subscription = Flux.from(bitcoinBlockPublishService).subscribe(val -> {
                try {
                    GetInfoResponse info = lndApi.getInfo();
                    log.info("=================================================");
                    log.info("[lnd] block height: {}", info.getBlockHeight());
                    log.info("[lnd] block hash: {}", info.getBlockHash());
                    log.info("[lnd] best header timestamp: {}", info.getBestHeaderTimestamp());
                } catch (StatusException | ValidationException e) {
                    log.error("", e);
                }
            });

            Runtime.getRuntime().addShutdownHook(new Thread(subscription::dispose));
        };
    }

    @Bean
    public ApplicationRunner channelEventUpdateRunner(AsynchronousLndAPI lndAsyncApi) {
        return args -> {
            Flux<ChannelEventUpdate> channelEventUpdates = Flux.create(emitter -> {
                try {
                    lndAsyncApi.subscribeChannelEvents(new StreamObserver<>() {
                        @Override
                        public void onNext(ChannelEventUpdate value) {
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

            Disposable channelEventUpdatesSubscription = channelEventUpdates
                    .onErrorContinue((error, obj) -> {
                        log.error("Error on value " + obj, error);
                    })
                    .subscribe(val -> {
                        log.info("=================================================");
                        log.info("[lnd] message_name: {}", val.getMessageName());
                        log.info("[lnd] type: {}", val.getType());
                    });

            Runtime.getRuntime().addShutdownHook(new Thread(channelEventUpdatesSubscription::dispose));
        };
    }


    @Bean
    public ApplicationRunner addInvoiceRunner() {
        return args -> {
            AddInvoiceResponse addInvoiceResponse = lndApi.addInvoice(new Invoice(LightningApi.Invoice.newBuilder()
                    .setMemo("Test invoice")
                    .build()));

            log.info("=================================================");
            log.info("[lnd] payment_request: {}", addInvoiceResponse.getPaymentRequest());
            log.info("[lnd] add_index: {}", addInvoiceResponse.getAddIndex());
            log.info("[lnd] message_name: {}", addInvoiceResponse.getMessageName());
        };
    }
}
