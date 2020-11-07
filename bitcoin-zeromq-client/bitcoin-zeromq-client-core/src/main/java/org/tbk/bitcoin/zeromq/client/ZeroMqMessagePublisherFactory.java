package org.tbk.bitcoin.zeromq.client;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.FlowAdapters;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;
import reactor.core.publisher.Flux;
import zmq.ZError;

import java.util.Optional;
import java.util.concurrent.Flow;

@Slf4j
@Value
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ZeroMqMessagePublisherFactory implements MessagePublisherFactory<byte[]> {

    @NonNull
    Topic topic;

    @NonNull
    String address;

    @Override
    public String getTopicName() {
        return topic.getName();
    }

    public Flux<byte[]> create() {
        Flux<byte[]> messageFlux = Flux.<byte[]>create(emitter -> {
            try (ZContext context = new ZContext()) {
                try (ZMQ.Socket socket = context.createSocket(SocketType.SUB)) {
                    boolean connected = socket.connect(address);
                    log.info("connected to address '{}': {}", address, connected);

                    boolean subscribed = socket.subscribe(topic.getName());
                    log.info("subscribed to '{}': {}", topic.getName(), subscribed);

                    while (!emitter.isCancelled()) {
                        byte[] rawTopicOrUnknown = socket.recv();
                        Optional<String> topicOrEmpty = Optional.ofNullable(rawTopicOrUnknown)
                                .map(val -> new String(val, ZMQ.CHARSET))
                                .filter(topic.getName()::equals);

                        if (topicOrEmpty.isPresent()) {
                            byte[] raw = socket.recv();

                            log.trace("publishing: {}", raw);

                            try {
                                emitter.next(raw);
                            } catch (Exception e) {
                                log.warn("", e);
                            }
                        }
                    }
                }

                emitter.complete();
            } catch (ZMQException e) {
                boolean threadInterrupted = e.getErrorCode() == ZError.EINTR;
                if (threadInterrupted) {
                    emitter.complete();
                } else {
                    log.error("", e);
                    emitter.error(e);
                }
            } catch (Exception e) {
                log.error("", e);
                emitter.error(e);
            }
        });

        return messageFlux;
    }

}
