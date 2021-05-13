package org.tbk.bitcoin.regtest.mining;

import org.bitcoinj.core.Sha256Hash;

import java.util.List;

public interface BitcoindRegtestMiner {

    default List<Sha256Hash> mineBlocks(int count) {
        return mineBlocks(1, new RegtestEaterAddressSupplier());
    }

    List<Sha256Hash> mineBlocks(int count, CoinbaseRewardAddressSupplier addressSupplier);

}

