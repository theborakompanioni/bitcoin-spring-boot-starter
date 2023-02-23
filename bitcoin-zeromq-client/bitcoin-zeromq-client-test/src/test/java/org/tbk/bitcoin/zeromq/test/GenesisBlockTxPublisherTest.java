package org.tbk.bitcoin.zeromq.test;

import org.junit.jupiter.api.Test;
import org.tbk.bitcoin.common.genesis.GenesisTx;
import org.tbk.bitcoin.zeromq.client.MessagePublisherFactory;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

class GenesisBlockTxPublisherTest {

    private static final MessagePublisherFactory<byte[]> sut = new GenesisBlockTxPublisher();

    @Test
    void itShouldPublishGenesisBlockTx() {
        List<byte[]> txs = sut.create().collectList().block();
        assertThat(txs, hasSize(1));
        assertThat(txs.get(0), is(GenesisTx.get().toByteArray()));
    }
}