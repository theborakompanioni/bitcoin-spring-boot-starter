package org.tbk.bitcoin.regtest.scenario;

import org.bitcoinj.core.Sha256Hash;
import org.reactivestreams.Subscriber;
import org.tbk.bitcoin.regtest.mining.RegtestMiner;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public final class MineBlockAction implements RegtestAction<List<Sha256Hash>> {
    private final RegtestMiner regtestMiner;
    private final int amountOfBlocks;

    public MineBlockAction(RegtestMiner regtestMiner) {
        this(regtestMiner, 1);
    }

    public MineBlockAction(RegtestMiner regtestMiner, int amountOfBlocks) {
        checkArgument(amountOfBlocks > 0, "'count' must be greater than zero");

        this.regtestMiner = requireNonNull(regtestMiner);
        this.amountOfBlocks = amountOfBlocks;
    }

    @Override
    public void subscribe(Subscriber<? super List<Sha256Hash>> s) {
        create().subscribe(s);

    }

    private Mono<List<Sha256Hash>> create() {
        return Mono.fromCallable(() -> this.regtestMiner.mineBlocks(this.amountOfBlocks));
    }
}
