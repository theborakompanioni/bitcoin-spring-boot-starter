package org.tbk.bitcoin.zeromq.bitcoinj;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Transaction;
import org.reactivestreams.FlowAdapters;
import org.tbk.bitcoin.zeromq.client.BitcoinZeroMqTopics;
import org.tbk.bitcoin.zeromq.client.ZeroMqMessagePublishService;
import org.tbk.bitcoin.zeromq.client.ZeroMqMessagePublisherFactory;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.LongAdder;

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
public class RawToTransactionTransformProcessorManualTest {
    public static void main(String[] args) throws TimeoutException {
        final ExecutorService publisherExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setNameFormat("btc-zmq-msg-pub--%d")
                .setDaemon(false)
                .build());

        ZeroMqMessagePublishService rawTxPublisher = new ZeroMqMessagePublishService(ZeroMqMessagePublisherFactory.builder()
                .topic(BitcoinZeroMqTopics.rawtx())
                .address("tcp://localhost:28333")
                .build());

        Flow.Processor<byte[], Transaction> transactionProcessor = RawToTransactionTransformProcessors.mainnetTxTransformer(publisherExecutor, Flow.defaultBufferSize());


        Flux.from(FlowAdapters.toPublisher(transactionProcessor))
                .buffer(Duration.ofSeconds(1))
                .subscribe(arg -> {
                    log.info("amount of tx observed {}", arg.size());
                });

        Flux.from(FlowAdapters.toPublisher(transactionProcessor))
                .name("buffered_zmq_btc_bicoinj_tx")
                .tag("buffer", Duration.ofSeconds(3).toString())
                .metrics()
                .buffer(Duration.ofSeconds(3))
                .subscribe(foo -> {
                    log.info("{}", Metrics.globalRegistry);
                    Metrics.globalRegistry.forEachMeter(val -> log.info("{}", val.measure()));
                });

        Flux.from(FlowAdapters.toPublisher(transactionProcessor))
                .limitRequest(3)
                .subscribe(arg -> {
                    log.info("just 3 {}", arg);
                });

        LongAdder counter = new LongAdder();
        Flux.from(FlowAdapters.toPublisher(transactionProcessor))
                .subscribe(arg -> {
                    counter.increment();
                    if (counter.longValue() > 10) {
                        throw new IllegalStateException();
                    }

                    log.info("{}", arg);
                });


        rawTxPublisher.subscribe(transactionProcessor);

        rawTxPublisher.startAsync();

        rawTxPublisher.awaitRunning(Duration.ofSeconds(10));

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
