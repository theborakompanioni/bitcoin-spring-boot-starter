package org.tbk.bitcoin.zeromq.test;

import org.tbk.bitcoin.common.genesis.GenesisBlock;
import org.tbk.bitcoin.zeromq.client.MessagePublisherFactory;
import reactor.core.publisher.Flux;

public final class GenesisBlockPublisher implements MessagePublisherFactory<byte[]> {

    @Override
    public String getTopicName() {
        return "tbk-test-rawblock";
    }

    @Override
    public Flux<byte[]> create() {
        return Flux.just(GenesisBlock.get().toByteArray());
    }
}
