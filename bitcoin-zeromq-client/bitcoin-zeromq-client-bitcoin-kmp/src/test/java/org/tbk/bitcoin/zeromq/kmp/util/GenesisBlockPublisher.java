package org.tbk.bitcoin.zeromq.kmp.util;

import org.tbk.bitcoin.common.util.GenesisBlock;
import org.tbk.bitcoin.zeromq.client.MessagePublisherFactory;
import reactor.core.publisher.Flux;

public class GenesisBlockPublisher implements MessagePublisherFactory<byte[]> {

    @Override
    public String getTopicName() {
        return "test-rawblock";
    }

    @Override
    public Flux<byte[]> create() {
        return Flux.just(GenesisBlock.get().toByteArray());
    }
}
