package org.tbk.bitcoin.regtest.scenario;

import org.bitcoinj.core.Sha256Hash;
import org.tbk.bitcoin.regtest.mining.CoinbaseRewardAddressSupplier;
import org.tbk.bitcoin.regtest.mining.RegtestMiner;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.Objects.requireNonNull;

public final class BitcoinRegtestActions {

    private final RegtestMiner regtestMiner;

    public BitcoinRegtestActions(RegtestMiner regtestMiner) {
        this.regtestMiner = requireNonNull(regtestMiner);
    }

    public MineBlockAction mineBlock() {
        return mineBlocks(1);
    }

    public MineBlockAction mineBlocks(int blocks) {
        return new MineBlockAction(this.regtestMiner, blocks);
    }

    public MineBlockWithCoinbaseAction mineBlockWithCoinbase(CoinbaseRewardAddressSupplier coinbaseRewardAddressSupplier) {
        return new MineBlockWithCoinbaseAction(this.regtestMiner, coinbaseRewardAddressSupplier);
    }

    /**
     * Funds the
     * @param coinbaseRewardAddressSupplier
     * @return
     */
    public RegtestAction<List<Sha256Hash>> fundAddress(CoinbaseRewardAddressSupplier coinbaseRewardAddressSupplier) {
        return (s) -> {
            Mono.from(mineBlockWithCoinbase(coinbaseRewardAddressSupplier))
                    .concatWith(mineBlocks(100))
                    .flatMapIterable(it -> it)
                    .collectList()
                    .subscribe(s);
        };
    }
}
