package org.tbk.bitcoin.zeromq.bitcoinj;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.reactivestreams.FlowAdapters;
import org.tbk.bitcoin.zeromq.client.MessagePublisherFactory;
import reactor.core.publisher.Flux;

import java.util.concurrent.Flow;

import static java.util.Objects.requireNonNull;

@Slf4j
public final class BitcoinjTransactionPublisherFactory implements MessagePublisherFactory<Transaction> {

    private final NetworkParameters networkParams;
    private final MessagePublisherFactory<byte[]> delegate;

    public BitcoinjTransactionPublisherFactory(NetworkParameters networkParams, MessagePublisherFactory<byte[]> publisherFactory) {
        this.networkParams = requireNonNull(networkParams);
        this.delegate = requireNonNull(publisherFactory);
    }

    @Override
    public String getTopicName() {
        return delegate.getTopicName();
    }

    @Override
    public Flow.Publisher<Transaction> create() {
        return FlowAdapters.toFlowPublisher(Flux.from(FlowAdapters.toPublisher(delegate.create()))
                .map(val -> new Transaction(networkParams, val)));
    }
}
