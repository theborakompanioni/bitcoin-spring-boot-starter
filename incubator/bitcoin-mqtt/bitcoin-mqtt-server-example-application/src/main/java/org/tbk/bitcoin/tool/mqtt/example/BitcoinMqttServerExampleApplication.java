package org.tbk.bitcoin.tool.mqtt.example;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Transaction;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.context.WebServerPortFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@SpringBootApplication
public class BitcoinMqttServerExampleApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(BitcoinMqttServerExampleApplication.class)
                .listeners(applicationPidFileWriter())
                .web(WebApplicationType.NONE)
                .profiles("development", "local")
                .run(args);
    }

    public static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner mainRunner(MessagePublishService<Transaction> bitcoinjTransactionPublishService) {
        return args -> {
            log.info("Starting example application main runner");

            Stopwatch statsStopwatch = Stopwatch.createStarted();
            Duration statsInterval = Duration.ofSeconds(10);
            AtomicLong statsTxCounter = new AtomicLong();

            Flux.from(bitcoinjTransactionPublishService)
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

            AtomicLong singleTxCounter = new AtomicLong();
            Flux.from(bitcoinjTransactionPublishService)
                    .subscribe(arg -> {
                        log.info("{} - {}", singleTxCounter.incrementAndGet(), arg);
                    });

            bitcoinjTransactionPublishService.awaitRunning(Duration.ofSeconds(10));
        };
    }

}
