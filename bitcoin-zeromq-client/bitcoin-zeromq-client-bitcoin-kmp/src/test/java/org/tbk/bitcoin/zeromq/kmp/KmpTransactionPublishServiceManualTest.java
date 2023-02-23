package org.tbk.bitcoin.zeromq.kmp;

import fr.acinq.bitcoin.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.zeromq.client.BitcoinZeroMqTopics;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.bitcoin.zeromq.client.ZeroMqMessagePublisherFactory;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * e.g. connect to your node:
 * ssh my_node.local -vvv -N -L 28332:localhost:28332 -L 28333:localhost:28333
 * <p>
 * Example Bitcoin Core settings:
 * # Enable publishing of raw block hex to <address>.
 * zmqpubrawblock=tcp://127.0.0.1:28332
 * # Enable publishing of raw transaction hex to <address>.
 * zmqpubrawtx=tcp://127.0.0.1:28333
 */
@Slf4j
public class KmpTransactionPublishServiceManualTest {
    public static void main(String[] args) throws TimeoutException {
        ZeroMqMessagePublisherFactory zmqRawTxPublisherFactory = ZeroMqMessagePublisherFactory.builder()
                .topic(BitcoinZeroMqTopics.rawtx())
                .address("tcp://localhost:28333")
                .build();

        KmpTransactionPublisherFactory txPublisherFactory = new KmpTransactionPublisherFactory(zmqRawTxPublisherFactory);

        MessagePublishService<Transaction> txPublishService = new MessagePublishService<>(txPublisherFactory);

        AtomicLong counter = new AtomicLong();

        Flux.from(txPublishService)
                .subscribe(arg -> {
                    log.info("{} - {}", counter.incrementAndGet(), arg);
                });

        txPublishService.startAsync();
        txPublishService.awaitRunning(Duration.ofSeconds(10));

        Flux.from(txPublishService)
                .buffer(Duration.ofSeconds(10))
                .subscribe(arg -> {
                    log.info("--------------------------- {}", arg.size());

                    txPublishService.stopAsync();
                });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                txPublishService.stopAsync();
                txPublishService.awaitTerminated(Duration.ofSeconds(10L));
            } catch (TimeoutException e) {
                log.error("", e);
            }
        }));
    }
}
