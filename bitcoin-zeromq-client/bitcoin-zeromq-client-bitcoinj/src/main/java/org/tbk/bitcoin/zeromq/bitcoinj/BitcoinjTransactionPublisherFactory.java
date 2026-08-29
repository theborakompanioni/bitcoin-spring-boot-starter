package org.tbk.bitcoin.zeromq.bitcoinj;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Transaction;
import org.tbk.bitcoin.zeromq.client.MessagePublisherFactory;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;

@Slf4j
public final class BitcoinjTransactionPublisherFactory implements MessagePublisherFactory<Transaction> {

    private final BitcoinSerializer bitcoinSerializer;
    private final MessagePublisherFactory<byte[]> delegate;

    public BitcoinjTransactionPublisherFactory(BitcoinSerializer bitcoinSerializer, MessagePublisherFactory<byte[]> publisherFactory) {
        this.bitcoinSerializer = requireNonNull(bitcoinSerializer);
        this.delegate = requireNonNull(publisherFactory);
    }

    @Override
    public String getTopicName() {
        return delegate.getTopicName();
    }

    @Override
    public Flux<Transaction> create() {
        return delegate.create()
                .map(ByteBuffer::wrap)
                .map(bitcoinSerializer::makeTransaction);
    }
}
