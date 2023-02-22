package org.tbk.bitcoin.zeromq.kmp;

import fr.acinq.bitcoin.Block;
import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.zeromq.client.MessagePublisherFactory;
import reactor.core.publisher.Flux;

import static java.util.Objects.requireNonNull;

@Slf4j
public final class KmpBlockPublisherFactory implements MessagePublisherFactory<Block> {

    private final MessagePublisherFactory<byte[]> delegate;

    public KmpBlockPublisherFactory(MessagePublisherFactory<byte[]> publisherFactory) {
        this.delegate = requireNonNull(publisherFactory);
    }

    @Override
    public String getTopicName() {
        return delegate.getTopicName();
    }

    @Override
    public Flux<Block> create() {
        return delegate.create()
                .map(Block::read);
    }
}
