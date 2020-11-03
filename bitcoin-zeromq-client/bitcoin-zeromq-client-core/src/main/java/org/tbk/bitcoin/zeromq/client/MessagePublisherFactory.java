package org.tbk.bitcoin.zeromq.client;

import java.util.concurrent.Flow;

public interface MessagePublisherFactory<T> {

    String getTopicName();

    Flow.Publisher<T> create();
}
