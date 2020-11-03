package org.tbk.bitcoin.zeromq.client;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Hexadecimals;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.reactivestreams.FlowAdapters;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.Flow;
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
public class ZeroMqMessagePublisherFactoryManualTest {
    public static void main(String[] args) {

        Flow.Publisher<byte[]> rawtx = ZeroMqMessagePublisherFactory.builder()
                .topic(BitcoinZeroMqTopics.rawtx())
                .address("tcp://localhost:28332")
                .build()
                .create();

        Flux<byte[]> autoRawTx = Flux.from(FlowAdapters.toPublisher(rawtx))
                .publish()
                .autoConnect();

        AtomicLong counter = new AtomicLong();

        log.info("start subscribing..");

        autoRawTx.subscribe(arg -> {
            log.info("{}, rawtx {}", counter.incrementAndGet(), Hexadecimals.toHexString(arg));
        });

        autoRawTx.map(val -> new Transaction(MainNetParams.get(), val))
                .subscribe(arg -> {
                    log.info("{}", arg);
                });

        autoRawTx.buffer(Duration.ofSeconds(3))
                .subscribe(arg -> {
                    log.info("{}", arg);
                });
    }
}
