package org.tbk.bitcoin.regtest;

import org.bitcoinj.core.Sha256Hash;

import java.util.List;

public interface BitcoindRegtestMiner {

    List<Sha256Hash> mineBlocks(int count);
}

