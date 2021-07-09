package org.tbk.bitcoin.zeromq.client;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * e.g. connect to your node:
 * ssh my_node.local -vvv -N -L 28332:localhost:28332 -L 28333:localhost:28333
 *
 * <p>Example Bitcoin Core settings:
 * # Enable publishing of raw block hex on address.
 * zmqpubrawblock=tcp://127.0.0.1:28332
 * # Enable publishing of raw transaction hex on address.
 * zmqpubrawtx=tcp://127.0.0.1:28333
 */
@Slf4j
public class MessagePublishServiceManualTest {
    public static void main(String[] args) throws TimeoutException {
        MessagePublishService<byte[]> rawTxPublisher = new MessagePublishService<>(ZeroMqMessagePublisherFactory.builder()
                .topic(BitcoinZeroMqTopics.rawtx())
                .address("tcp://localhost:28333")
                .build());

        AtomicLong counter = new AtomicLong();

        Flux.from(rawTxPublisher)
                .subscribe(arg -> {
                    log.info("{} - {}", counter.incrementAndGet(), arg);
                });

        rawTxPublisher.startAsync();
        rawTxPublisher.awaitRunning(Duration.ofSeconds(10));

        Flux.from(rawTxPublisher)
                .buffer(Duration.ofSeconds(10))
                .subscribe(arg -> {
                    log.info("--------------------------- {}", arg.size());

                    rawTxPublisher.stopAsync();
                });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                rawTxPublisher.stopAsync();
                rawTxPublisher.awaitTerminated(Duration.ofSeconds(10L));
            } catch (TimeoutException e) {
                log.error("", e);
            }
        }));
    }
}
