package org.tbk.spring.testcontainer.cln.example;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.lightning.cln.grpc.client.*;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HexFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@SpringBootApplication
public class ClnContainerExampleApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(ClnContainerExampleApplication.class)
                .listeners(applicationPidFileWriter())
                .web(WebApplicationType.NONE)
                .profiles("development", "local")
                .run(args);
    }

    public static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }

    private final NodeGrpc.NodeFutureStub clnNodeFutureStub;

    public ClnContainerExampleApplication(NodeGrpc.NodeFutureStub clnNodeFutureStub) {
        this.clnNodeFutureStub = requireNonNull(clnNodeFutureStub);
    }

    @Bean
    @Profile("!test")
    public ApplicationRunner clnPrintInfoRunner() {
        return args -> {
            GetinfoResponse info = clnNodeFutureStub.getinfo(GetinfoRequest.newBuilder().build())
                    .get(10, TimeUnit.SECONDS);
            log.info("=================================================");
            log.info("[cln] id: {}", HexFormat.of().formatHex(info.getId().toByteArray()));
            log.info("[cln] alias: {}", info.getAlias());
            log.info("[cln] version: {}", info.getVersion());
            log.info("[cln] network: {}", info.getNetwork());
            log.info("[cln] bindings: {}", info.getBindingList().stream()
                    .map(it -> String.format("%s:%d (%s)", it.getAddress(), it.getPort(), it.getItemType().name()))
                    .collect(Collectors.joining("; "))
            );
        };
    }

    @Bean
    @Profile("!test")
    public ApplicationRunner clnBestBlockLogger(MessagePublishService<Block> bitcoinBlockPublishService) {
        return args -> {
            bitcoinBlockPublishService.awaitRunning(Duration.ofSeconds(20));
            Disposable subscription = Flux.from(bitcoinBlockPublishService).subscribe(val -> {
                try {
                    GetinfoResponse info = clnNodeFutureStub.getinfo(GetinfoRequest.newBuilder().build())
                            .get(10, TimeUnit.SECONDS);
                    log.info("=================================================");
                    log.info("[cln] block height: {}", info.getBlockheight());
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    log.error("", e);
                }
            });

            Runtime.getRuntime().addShutdownHook(new Thread(subscription::dispose));
        };
    }

    @Bean
    @Profile("!test")
    public ApplicationRunner clnAddTestInvoiceRunner() {
        return args -> {
            InvoiceResponse invoiceResponse = clnNodeFutureStub.invoice(InvoiceRequest.newBuilder()
                            .setAmountMsat(AmountOrAny.newBuilder()
                                    .setAmount(Amount.newBuilder()
                                            .setMsat(21_000_000)
                                            .build())
                                    .build())
                            .setDescription("Test invoice")
                            .setLabel("test")
                            .build())
                    .get(10, TimeUnit.SECONDS);

            log.info("=================================================");
            log.info("[cln] payment_hash: {}", HexFormat.of().formatHex(invoiceResponse.getPaymentHash().toByteArray()));
            log.info("[cln] bolt11: {}", invoiceResponse.getBolt11());
            log.info("[cln] expires_at: {}", invoiceResponse.getExpiresAt());
        };
    }
}
