package org.tbk.bitcoin.zeromq.bitcoinj;

import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;
import org.tbk.bitcoin.zeromq.client.MessagePublisherFactory;
import org.tbk.bitcoin.zeromq.test.GenesisBlockTxPublisher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class BitcoinjTransactionPublisherFactoryTest {
    private static final MessagePublisherFactory<byte[]> genesisTxPublisher = new GenesisBlockTxPublisher();

    private static final BitcoinSerializer mainnetSerializer = new BitcoinSerializer(MainNetParams.get(), false);

    @Test
    void create() {
        BitcoinjTransactionPublisherFactory sut = new BitcoinjTransactionPublisherFactory(mainnetSerializer, genesisTxPublisher);

        Transaction tx = sut.create()
                .blockFirst();

        assertThat(tx, is(notNullValue()));
        assertThat(tx.getTxId(), is(Sha256Hash.wrap("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b")));

        assertThat(tx.getInputs(), hasSize(1));

        TransactionInput txIn = tx.getInputs().get(0);
        assertThat(txIn.getScriptBytes(), is(Hex.decode("04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73")));
        assertThat(txIn.getSequenceNumber(), is(4294967295L));

        assertThat(tx.getOutputs(), hasSize(1));

        TransactionOutput txOut = tx.getOutputs().get(0);
        assertThat(txOut.getValue(), is(Coin.valueOf(50_00_000_000L)));
        assertThat(txOut.getScriptBytes(), is(Hex.decode("4104678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5fac")));
    }
}