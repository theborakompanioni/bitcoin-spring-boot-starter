package org.tbk.bitcoin.regtest.scenario;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Sha256Hash;
import org.reactivestreams.Subscriber;
import org.tbk.bitcoin.regtest.mining.CoinbaseRewardAddressSupplier;
import org.tbk.bitcoin.regtest.mining.RegtestMiner;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
public final class MineBlockWithCoinbaseAction implements RegtestAction<List<Sha256Hash>> {

    private final RegtestMiner regtestMiner;
    private final CoinbaseRewardAddressSupplier coinbaseRewardAddressSupplier;
    private final int blocks;

    public MineBlockWithCoinbaseAction(RegtestMiner regtestMiner,
                                       CoinbaseRewardAddressSupplier coinbaseRewardAddressSupplier) {
        this(regtestMiner, coinbaseRewardAddressSupplier, 1);
    }

    public MineBlockWithCoinbaseAction(RegtestMiner regtestMiner,
                                       CoinbaseRewardAddressSupplier coinbaseRewardAddressSupplier,
                                       int blocks) {
        this.blocks = blocks;
        checkArgument(blocks > 0, "'blocks' must be greater than or equal to zero");

        this.regtestMiner = requireNonNull(regtestMiner);
        this.coinbaseRewardAddressSupplier = requireNonNull(coinbaseRewardAddressSupplier);
    }

    @Override
    public void subscribe(Subscriber<? super List<Sha256Hash>> s) {
        create().subscribe(s);
    }

    private Mono<List<Sha256Hash>> create() {
        return Mono.fromCallable(() -> this.regtestMiner.mineBlocks(blocks, this.coinbaseRewardAddressSupplier));
    }
}
