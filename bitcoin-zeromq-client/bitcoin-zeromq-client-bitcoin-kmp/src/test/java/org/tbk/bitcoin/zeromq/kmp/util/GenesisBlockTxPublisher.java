package org.tbk.bitcoin.zeromq.kmp.util;

import fr.acinq.bitcoin.Block;
import fr.acinq.bitcoin.Transaction;
import org.tbk.bitcoin.zeromq.client.MessagePublisherFactory;
import reactor.core.publisher.Flux;

public class GenesisBlockTxPublisher implements MessagePublisherFactory<byte[]> {
    private final static MessagePublisherFactory<byte[]> genesisBlockPublisher = new GenesisBlockPublisher();

    @Override
    public String getTopicName() {
        return "test-rawblock";
    }

    @Override
    public Flux<byte[]> create() {
        return genesisBlockPublisher.create()
                .map(Block::read)
                .flatMap(it -> Flux.fromIterable(it.tx))
                .map(Transaction::write);
    }
}
