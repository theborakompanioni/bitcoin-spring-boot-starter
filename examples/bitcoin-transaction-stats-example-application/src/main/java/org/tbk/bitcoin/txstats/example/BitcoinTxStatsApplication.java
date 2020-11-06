package org.tbk.bitcoin.txstats.example;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Transaction;
import org.reactivestreams.FlowAdapters;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.context.WebServerPortFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.xchange.jsr354.XChangeExchangeRateProvider;
import reactor.core.publisher.Flux;

import javax.money.Monetary;
import javax.money.convert.*;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@SpringBootApplication
public class BitcoinTxStatsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(BitcoinTxStatsApplication.class)
                .listeners(applicationPidFileWriter(), webServerPortFileWriter())
                .web(WebApplicationType.SERVLET)
                .profiles("development", "local")
                .run(args);
    }

    public static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }

    public static ApplicationListener<?> webServerPortFileWriter() {
        return new WebServerPortFileWriter("application.port");
    }

    @Bean
    public CommandLineRunner mainRunner(MessagePublishService<Transaction> bitcoinjTransactionPublishService) {
        return args -> {
            log.info("Starting example application main runner");

            Stopwatch statsStopwatch = Stopwatch.createStarted();
            Duration statsInterval = Duration.ofSeconds(10);
            AtomicLong statsTxCounter = new AtomicLong();

            Flux.from(FlowAdapters.toPublisher(bitcoinjTransactionPublishService))
                    .buffer(statsInterval)
                    .subscribe(arg -> {
                        statsTxCounter.addAndGet(arg.size());

                        long intervalElapsedSeconds = Math.max(statsInterval.toSeconds(), 1);
                        long statsTotalElapsedSeconds = Math.max(statsStopwatch.elapsed(TimeUnit.SECONDS), 1);

                        log.info("=======================================");
                        log.info("elapsed: {}", statsStopwatch);
                        log.info("tx count last {} seconds: {}", intervalElapsedSeconds, arg.size());
                        log.info("tx/s last {} seconds: {}", intervalElapsedSeconds, arg.size() / (float) intervalElapsedSeconds);
                        log.info("total tx count: {}", statsTxCounter.get());
                        log.info("total tx/s: {}", statsTxCounter.get() / (float) statsTotalElapsedSeconds);
                        log.info("=======================================");
                    });

            bitcoinjTransactionPublishService.awaitRunning(Duration.ofSeconds(10));
        };
    }

    @Bean
    public CommandLineRunner exchangeRateDemoRunner() {
        ConversionQuery conversionQuery = ConversionQueryBuilder.of()
                .setBaseCurrency(Monetary.getCurrency("BTC"))
                .setTermCurrency(Monetary.getCurrency("USD"))
                .build();

        return args -> {
            Flux.interval(Duration.ofSeconds(30))
                    .subscribe(foo -> {
                        log.info("======================================================");

                        ExchangeRateProvider exchangeRateProvider = MonetaryConversions.getExchangeRateProvider(conversionQuery);
                        ExchangeRate exchangeRate = exchangeRateProvider.getExchangeRate(conversionQuery);

                        log.info("exchangeRate: {}", exchangeRate);
                        log.info("======================================================");
                    });
        };
    }

}
