package org.tbk.bitcoin.zeromq.kmp;

import fr.acinq.bitcoin.*;
import org.junit.jupiter.api.Test;
import org.tbk.bitcoin.zeromq.client.MessagePublisherFactory;
import org.tbk.bitcoin.zeromq.test.GenesisBlockTxPublisher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class KmpTransactionPublisherFactoryTest {
    private static final MessagePublisherFactory<byte[]> genesisTxPublisher = new GenesisBlockTxPublisher();

    @Test
    void create() {
        KmpTransactionPublisherFactory sut = new KmpTransactionPublisherFactory(genesisTxPublisher);

        Transaction tx = sut.create()
                .blockFirst();

        assertThat(tx, is(notNullValue()));
        assertThat(tx.txid, is(new TxId("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b")));
        assertThat(tx.hash, is(new TxHash("3ba3edfd7a7b12b27ac72c3e67768f617fc81bc3888a51323a9fb8aa4b1e5e4a")));

        assertThat(tx.txIn, hasSize(1));

        TxIn txIn = tx.txIn.get(0);
        assertThat(txIn.signatureScript, is(ByteVector.fromHex("04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73")));
        assertThat(new String(txIn.signatureScript.toByteArray()), endsWith("The Times 03/Jan/2009 Chancellor on brink of second bailout for banks"));
        assertThat(txIn.sequence, is(4294967295L));

        assertThat(tx.txOut, hasSize(1));

        TxOut txOut = tx.txOut.get(0);
        assertThat(txOut.amount, is(new Satoshi(50_00_000_000L)));
        assertThat(txOut.publicKeyScript, is(ByteVector.fromHex("4104678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5fac")));
    }
}