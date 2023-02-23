package org.tbk.bitcoin.zeromq.test;

import org.tbk.bitcoin.common.genesis.GenesisTx;
import org.tbk.bitcoin.zeromq.client.MessagePublisherFactory;
import reactor.core.publisher.Flux;

public final class GenesisBlockTxPublisher implements MessagePublisherFactory<byte[]> {

    @Override
    public String getTopicName() {
        return "tbk-test-rawtx";
    }

    @Override
    public Flux<byte[]> create() {
        return Flux.just(GenesisTx.get().toByteArray());
    }
}
