package org.tbk.bitcoin.regtest.electrum.faucet;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.tbk.bitcoin.regtest.common.AddressSupplier;
import org.tbk.bitcoin.regtest.electrum.scenario.ElectrumRegtestActions;
import org.tbk.bitcoin.regtest.scenario.BitcoinRegtestActions;
import org.tbk.electrum.bitcoinj.BitcoinjElectrumClient;
import org.tbk.electrum.model.History;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static java.util.Objects.requireNonNull;

@Slf4j
public class SimpleElectrumRegtestFaucet implements ElectrumRegtestFaucet {
    // e.g. below electrum will throw "TxBroadcastServerReturnedError: Transaction could not be broadcast due to dust outputs."
    private static final Coin minAllowedAmountPerRequest = Coin.SATOSHI.multiply(1000);
    private static final Coin maxAllowedAmountPerRequest = Coin.COIN.multiply(100);
    private static final Coin txFee = Coin.valueOf(50_000L);

    private final BitcoinjElectrumClient electrumClient;
    private final BitcoinRegtestActions bitcoinRegtestActions;
    private final ElectrumRegtestActions electrumRegtestActions;

    public SimpleElectrumRegtestFaucet(BitcoinjElectrumClient electrumClient,
                                       BitcoinRegtestActions bitcoinRegtestActions) {
        this.electrumClient = requireNonNull(electrumClient);
        this.bitcoinRegtestActions = requireNonNull(bitcoinRegtestActions);
        this.electrumRegtestActions = new ElectrumRegtestActions(electrumClient);
    }

    @Override
    public Mono<Sha256Hash> requestBitcoin(AddressSupplier destinationAddress, Coin amount) {
        checkAmount(amount);

        Coin neededSpendableAmount = amount.plus(txFee);

        Mono<Address> rewardAddress = Mono.fromCallable(electrumClient::listAddresses)
                .flatMapIterable(it -> it)
                .next()
                .cache();

        Mono<Address> fundWithCoinbaseReward = rewardAddress
                .flatMap(address -> Mono.from(bitcoinRegtestActions.mineBlockWithCoinbase(() -> address, 101))
                        .thenReturn(address));

        // this "workaround" waits for electrum to finish processing block updates
        // we cannot use something like "awaitSpendableBalance" to wait here because we do not know
        // the amount of the current block rewards so we workaround by waiting for an update and then
        // checking if we have enough funds available
        Mono<Integer> awaitBlockchainHeightIncrease = Mono.fromCallable(() -> {
            int currentBlockchainHeight = this.electrumClient.delegate().getInfo().getBlockchainHeight();

            return Flux.interval(Duration.ofMillis(100))
                    .doOnNext(it -> log.trace("Waiting for wallet to receive new blocks.. ({} attempt)", it))
                    .map(it -> this.electrumClient.delegate().getInfo().getBlockchainHeight())
                    .filter(newBlockchainHeight -> newBlockchainHeight > currentBlockchainHeight)
                    .blockFirst(Duration.ofSeconds(30));
        });

        return Mono.from(electrumRegtestActions.awaitWalletSynchronized(Duration.ofSeconds(10)))
                .map(it -> electrumClient.getBalance().getSpendable())
                .filter(spendable -> {
                    boolean hasEnoughFunds = !spendable.isLessThan(neededSpendableAmount);
                    log.debug("does the faucet control enough funds? {} (spendable {} less than {} needed)",
                            hasEnoughFunds, spendable.toFriendlyString(), neededSpendableAmount.toFriendlyString());
                    return hasEnoughFunds;
                })
                // mine a new coinbase reward to an address the electrum client is in control of
                .switchIfEmpty(fundWithCoinbaseReward
                        .flatMap(address -> Mono.from(awaitBlockchainHeightIncrease))
                        .map(blockheight -> electrumClient.getBalance().getSpendable()))
                .repeat()
                .takeWhile(spendable -> {
                    boolean mineMoreBlocks = spendable.isLessThan(neededSpendableAmount);
                    log.debug("does the faucet needs more coinbase rewards? {} (spendable {} less than {} needed)",
                            mineMoreBlocks, spendable.toFriendlyString(), neededSpendableAmount.toFriendlyString());
                    return mineMoreBlocks;
                })
                .collectList()
                .flatMap(receivedAmount -> Mono.from(electrumRegtestActions.sendPaymentAndAwaitTx(destinationAddress.get(), amount, txFee)))
                .map(History.Transaction::getTxHash)
                .map(Sha256Hash::wrap);
    }

    private void checkAmount(Coin amount) {
        boolean violatesUpperBound = amount.isGreaterThan(maxAllowedAmountPerRequest);
        if (violatesUpperBound) {
            String errorMessage = String.format("Cannot request more than %s from this faucet - got %s",
                    maxAllowedAmountPerRequest.toFriendlyString(), amount.toFriendlyString());
            throw new IllegalArgumentException(errorMessage);
        }
        boolean violatesLowerBound = amount.isLessThan(minAllowedAmountPerRequest);
        if (violatesLowerBound) {
            String errorMessage = String.format("Cannot request less than %s from this faucet - got %s",
                    minAllowedAmountPerRequest.toFriendlyString(), amount.toFriendlyString());
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
