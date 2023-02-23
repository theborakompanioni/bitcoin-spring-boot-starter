package org.tbk.bitcoin.zeromq.bitcoinj;

import com.google.common.base.MoreObjects;
import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;
import org.tbk.bitcoin.zeromq.client.MessagePublisherFactory;
import org.tbk.bitcoin.zeromq.test.GenesisBlockPublisher;

import java.sql.Date;
import java.time.Instant;
import java.util.Collections;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class BitcoinjBlockPublisherFactoryTest {
    private final static MessagePublisherFactory<byte[]> genesisBlockPublisher = new GenesisBlockPublisher();

    private final static Context bitcoinjContext = Context.getOrCreate(MainNetParams.get());

    private final static BitcoinSerializer mainnetSerializer  = new BitcoinSerializer(bitcoinjContext.getParams(), false);

    @Test
    void create() {
        BitcoinjBlockPublisherFactory sut = new BitcoinjBlockPublisherFactory(mainnetSerializer, genesisBlockPublisher);

        Block block = sut.create()
                .blockFirst();

        assertThat(block, is(notNullValue()));
        assertThat(block.getHash(), is(Sha256Hash.wrap("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f")));

        assertThat(block.getPrevBlockHash(), is(Sha256Hash.wrap("0000000000000000000000000000000000000000000000000000000000000000")));
        assertThat(block.getTime(), is(Date.from(Instant.ofEpochMilli(1231006505000L))));
        assertThat(block.getNonce(), is(2083236893L));

        assertThat(block.getTransactions(), hasSize(1));

        Transaction tx = firstNonNull(block.getTransactions(), Collections.<Transaction>emptyList()).get(0);
        assertThat(tx.getTxId(), is(Sha256Hash.wrap("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b")));

        TransactionInput txIn = tx.getInputs().get(0);
        assertThat(txIn.getScriptBytes(), is(Hex.decode("04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73")));
        assertThat(txIn.getSequenceNumber(), is(4294967295L));

        assertThat(tx.getOutputs(), hasSize(1));

        TransactionOutput txOut = tx.getOutputs().get(0);
        assertThat(txOut.getValue(), is(Coin.valueOf(50_00_000_000L)));
        assertThat(txOut.getScriptBytes(), is(Hex.decode("4104678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5fac")));
    }
}