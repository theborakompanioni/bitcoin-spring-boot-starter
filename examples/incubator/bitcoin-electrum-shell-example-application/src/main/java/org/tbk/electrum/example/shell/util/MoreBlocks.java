package org.tbk.electrum.example.shell.util;

import fr.acinq.bitcoin.Block;

public final class MoreBlocks {

    private MoreBlocks() {
        throw new UnsupportedOperationException();
    }

    public static Block toNetwork(String network) {
        return switch (network) {
            case "regtest" -> Block.RegtestGenesisBlock;
            case "signet" -> Block.SignetGenesisBlock;
            case "testnet", "testnet4" -> Block.Testnet4GenesisBlock;
            default -> Block.LivenetGenesisBlock;
        };
    }
}
