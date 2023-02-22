package org.tbk.bitcoin.zeromq.kmp;

import fr.acinq.bitcoin.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.zeromq.client.MessagePublisherFactory;
import reactor.core.publisher.Flux;

import static java.util.Objects.requireNonNull;

@Slf4j
public final class KmpTransactionPublisherFactory implements MessagePublisherFactory<Transaction> {

    private final MessagePublisherFactory<byte[]> delegate;

    public KmpTransactionPublisherFactory(MessagePublisherFactory<byte[]> publisherFactory) {
        this.delegate = requireNonNull(publisherFactory);
    }

    @Override
    public String getTopicName() {
        return delegate.getTopicName();
    }

    @Override
    public Flux<Transaction> create() {
        return delegate.create()
                .map(Transaction::read);
    }
}
