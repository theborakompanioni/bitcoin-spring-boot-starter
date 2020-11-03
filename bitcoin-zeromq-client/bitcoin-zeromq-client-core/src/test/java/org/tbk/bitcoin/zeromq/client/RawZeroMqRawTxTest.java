package org.tbk.bitcoin.zeromq.client;

import lombok.extern.slf4j.Slf4j;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.LongAdder;

/**
 * e.g. connect to your node:
 * ssh my_node.local -vvv -N -L 28332:localhost:28332 -L 28333:localhost:28333
 */
@Slf4j
public class RawZeroMqRawTxTest {
    private static final String TOPIC = "rawtx";

    /**
     * # Enable publishing of raw block hex to <address>.
     * zmqpubrawblock=tcp://127.0.0.1:28332
     * # Enable publishing of raw transaction hex to <address>.
     * zmqpubrawtx=tcp://127.0.0.1:28333
     *
     * @param args
     */

    public static void main(String[] args) {
        Flux<byte[]> objectFlux = Flux.<byte[]>create(emitter -> {
            try (ZContext context = new ZContext()) {
                try (ZMQ.Socket socket = context.createSocket(SocketType.SUB)) {
                    boolean connected = socket.connect("tcp://localhost:28333");
                    boolean subscribed = socket.subscribe(TOPIC);

                    log.info("connected: {}", connected);
                    log.info("subscribed: {}", subscribed);

                    while (!emitter.isCancelled()) {
                        byte[] rawTopicOrUnknown = socket.recv();
                        Optional<String> topicOrEmpty = Optional.of(rawTopicOrUnknown)
                                .map(String::new)
                                .filter(TOPIC::equals);
                        if (topicOrEmpty.isPresent()) {
                            byte[] raw = socket.recv();
                            log.info("publishing: {}", raw);

                            try {
                                emitter.next(raw);
                            } catch (Exception e) {
                                // ignore client exception
                            }
                        }
                    }
                }

                emitter.complete();
            } catch (Exception e) {
                log.error("", e);
                emitter.error(e);
            }
        })
                ;

        Flux<byte[]> connectableFlux = objectFlux
                .publishOn(Schedulers.newSingle("pub-on"))
                .subscribeOn(Schedulers.newSingle("sub-on"))
                .publish()
                .autoConnect(1);

        connectableFlux.subscribe(val -> {
            log.info("val1: {}", val);
        });

        connectableFlux.subscribe(val -> {
            log.info("val2: {}", val);
        });


        LongAdder longAdder = new LongAdder();
        connectableFlux.subscribe(val -> {
            longAdder.increment();
            log.info("val3: {}", val);

            if (longAdder.longValue() > 10) {
                throw new IllegalStateException();
            }
        });
    }

    public static void main2(String[] args) {
        Flux<byte[]> objectFlux = Flux.<byte[]>create(emitter -> {
            try (ZContext context = new ZContext()) {
                try (ZMQ.Socket socket = context.createSocket(SocketType.SUB)) {
                    boolean connected = socket.connect("tcp://localhost:28333");
                    boolean subscribed = socket.subscribe(TOPIC);

                    log.info("connected: {}", connected);
                    log.info("subscribed: {}", subscribed);

                    while (!emitter.isCancelled()) {
                        byte[] rawTopicOrUnknown = socket.recv();
                        Optional<String> topicOrEmpty = Optional.of(rawTopicOrUnknown)
                                .map(String::new)
                                .filter(TOPIC::equals);
                        if (topicOrEmpty.isPresent()) {
                            byte[] raw = socket.recv();
                            emitter.next(raw);
                        }
                    }
                }

                emitter.complete();
            } catch (Exception e) {
                emitter.error(e);
            }
        });

       ConnectableFlux<byte[]> connectableFlux = objectFlux
               .publishOn(Schedulers.newSingle("pub-on"))
               .subscribeOn(Schedulers.newSingle("sub-on"))
               .publish();

        connectableFlux.subscribe(val -> {
            log.info("val1: {}", val);
        });

        connectableFlux.subscribe(val -> {
            log.info("val2: {}", val);
        });


        connectableFlux.subscribe(val -> {
            log.info("val3: {}", val);
        });

        connectableFlux.connect();
    }
}
