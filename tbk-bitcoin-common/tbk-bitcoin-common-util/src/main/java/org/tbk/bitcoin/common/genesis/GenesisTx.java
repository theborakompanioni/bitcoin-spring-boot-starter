package org.tbk.bitcoin.common.genesis;

import org.tbk.bitcoin.common.util.Hex;

import java.util.Arrays;

public final class GenesisTx {

    private static final GenesisTx INSTANCE = GenesisTx.from(GenesisBlock.get());

    public static GenesisTx from(GenesisBlock genesisBlock) {
        byte[] genesisBlockRaw = genesisBlock.toByteArray();
        return new GenesisTx(Arrays.copyOfRange(genesisBlockRaw, 81, genesisBlockRaw.length));
    }

    public static GenesisTx get() {
        return INSTANCE;
    }

    private final byte[] raw;

    private GenesisTx(byte[] raw) {
        this.raw = Arrays.copyOfRange(raw, 0, raw.length);
    }

    public byte[] toByteArray() {
        return Arrays.copyOfRange(raw, 0, raw.length);
    }

    @Override
    public String toString() {
        return Hex.encode(raw);
    }
}

