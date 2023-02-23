package org.tbk.bitcoin.zeromq.test;

import org.junit.jupiter.api.Test;
import org.tbk.bitcoin.common.genesis.GenesisBlock;
import org.tbk.bitcoin.zeromq.client.MessagePublisherFactory;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

class GenesisBlockPublisherTest {

    private static final MessagePublisherFactory<byte[]> sut = new GenesisBlockPublisher();

    @Test
    void itShouldPublishGenesisBlock() {
        List<byte[]> blocks = sut.create().collectList().block();
        assertThat(blocks, hasSize(1));
        assertThat(blocks.get(0), is(GenesisBlock.get().toByteArray()));
    }
}