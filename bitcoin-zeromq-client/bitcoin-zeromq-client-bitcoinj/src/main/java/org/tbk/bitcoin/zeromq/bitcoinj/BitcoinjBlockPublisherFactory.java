package org.tbk.bitcoin.zeromq.bitcoinj;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Block;
import org.tbk.bitcoin.zeromq.client.MessagePublisherFactory;
import reactor.core.publisher.Flux;

import static java.util.Objects.requireNonNull;

@Slf4j
public final class BitcoinjBlockPublisherFactory implements MessagePublisherFactory<Block> {

    private final BitcoinSerializer bitcoinSerializer;
    private final MessagePublisherFactory<byte[]> delegate;

    public BitcoinjBlockPublisherFactory(BitcoinSerializer bitcoinSerializer, MessagePublisherFactory<byte[]> publisherFactory) {
        this.bitcoinSerializer = requireNonNull(bitcoinSerializer);
        this.delegate = requireNonNull(publisherFactory);
    }

    @Override
    public String getTopicName() {
        return delegate.getTopicName();
    }

    @Override
    public Flux<Block> create() {
        return delegate.create()
                .map(bitcoinSerializer::makeBlock);
    }
}
