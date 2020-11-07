package org.tbk.bitcoin.zeromq.client;

import reactor.core.publisher.Flux;

public interface MessagePublisherFactory<T> {

    String getTopicName();

    Flux<T> create();
}
