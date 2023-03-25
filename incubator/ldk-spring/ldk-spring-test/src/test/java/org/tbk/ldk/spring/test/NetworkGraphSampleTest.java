package org.tbk.ldk.spring.test;

import org.junit.jupiter.api.Test;
import org.ldk.enums.Network;
import org.ldk.impl.bindings;
import org.ldk.structs.BestBlock;
import org.ldk.structs.Logger;
import org.ldk.structs.NetworkGraph;
import org.ldk.structs.Record;

import java.util.HexFormat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class NetworkGraphSampleTest {

    @Test
    void itShouldImplementNetworkGraphExample() {
        String ldk_version = bindings.get_ldk_version(); // just load libraries

        Network network = Network.LDKNetwork_Regtest;

        BestBlock genesisBlock = BestBlock.from_network(network);
        byte[] genesisBlockHash = genesisBlock.block_hash();
        assertThat(HexFormat.of().formatHex(genesisBlockHash), is("06226e46111a0b59caaf126043eb5bbf28c34f3a5e332a1fc7b2b73cf188910f"));

        Logger logger = Logger.new_impl(new YourLogger());
        NetworkGraph networkGraph = NetworkGraph.of(network, logger);

        assertThat(networkGraph, is(notNullValue()));
    }

    private static class YourLogger implements Logger.LoggerInterface {
        @Override
        public void log(Record record) {
            // <insert code to print this log and/or write this log to a file>
        }
    }


}