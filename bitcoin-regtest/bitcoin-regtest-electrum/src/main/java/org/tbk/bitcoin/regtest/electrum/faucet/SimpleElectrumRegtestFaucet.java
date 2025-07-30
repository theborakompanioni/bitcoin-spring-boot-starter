package org.tbk.bitcoin.regtest.electrum.faucet;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.tbk.bitcoin.regtest.common.AddressSupplier;
import org.tbk.bitcoin.regtest.electrum.scenario.ElectrumRegtestActions;
import org.tbk.bitcoin.regtest.scenario.BitcoinRegtestActions;
import org.tbk.electrum.bitcoinj.BitcoinjElectrumClient;
import org.tbk.electrum.common.ListAddressParams;
import org.tbk.electrum.common.WalletParams;
import org.tbk.electrum.model.OnchainHistory;
import org.tbk.electrum.rpc.command.CreateParams;
import org.tbk.electrum.rpc.command.GetBalanceParams;
import org.tbk.electrum.rpc.command.LoadWalletParams;
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
    private final WalletParams walletParams;

    public SimpleElectrumRegtestFaucet(BitcoinjElectrumClient electrumClient,
                                       BitcoinRegtestActions bitcoinRegtestActions,
                                       WalletParams walletParams) {
        this.electrumClient = requireNonNull(electrumClient);
        this.bitcoinRegtestActions = requireNonNull(bitcoinRegtestActions);
        this.walletParams = requireNonNull(walletParams);

        this.electrumRegtestActions = new ElectrumRegtestActions(electrumClient);
    }

    private void createWalletIfNecessaryOrThrow() {
        LoadWalletParams loadWalletParams = LoadWalletParams.builder()
                .walletPath(walletParams.getWalletPath())
                .password(walletParams.getPassword().orElse(null))
                .build();

        boolean walletAlreadyLoaded = tryLoadWallet(loadWalletParams);
        if (!walletAlreadyLoaded) {
            this.electrumClient.delegate().createWallet(CreateParams.builder()
                    .walletPath(walletParams.getWalletPath())
                    .password(walletParams.getPassword().orElse(null))
                    .passphrase("faucet")
                    .encryptFile(walletParams.getPassword().isPresent())
                    .build());
            boolean walletLoadedAfterCreation = tryLoadWallet(loadWalletParams);
            if (!walletLoadedAfterCreation) {
                throw new IllegalStateException("Cannot load faucet wallet '%s'".formatted(loadWalletParams.getWalletPath()));
            }
        }
    }

    private boolean tryLoadWallet(LoadWalletParams params) {
        try {
            return this.electrumClient.delegate().loadWallet(params);
        } catch (Exception e) {
            log.trace("Exception while trying to load wallet: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Mono<Sha256Hash> requestBitcoin(AddressSupplier destinationAddress, Coin amount) {
        checkAmount(amount);

        createWalletIfNecessaryOrThrow();

        Coin neededSpendableAmount = amount.plus(txFee);

        Mono<Address> rewardAddress = Mono.fromCallable(() -> electrumClient.listAddresses(ListAddressParams.builder()
                        .walletPath(walletParams.getWalletPath())
                        .unused(true)
                        .receiving(true)
                        .build()))
                .flatMapIterable(it -> it)
                .next()
                .cache();

        Mono<Address> fundWithCoinbaseReward = rewardAddress
                .flatMap(address -> Mono.from(bitcoinRegtestActions.mineBlockWithCoinbase(() -> address, 101))
                        .thenReturn(address));

        // this "workaround" waits for electrum to finish processing block updates
        // we cannot use something like "awaitSpendableBalance" to wait here because we do not know
        // the amount of the current block rewards so we work around by waiting for an update and then
        // checking if we have enough funds available
        Mono<Integer> awaitBlockchainHeightIncrease = Mono.fromCallable(() -> {
            int currentServerHeight = this.electrumClient.delegate().getInfo().getServerHeight();

            return Flux.interval(Duration.ofMillis(100))
                    .doOnNext(it -> log.trace("Waiting for wallet to receive new blocks.. ({} attempt)", it))
                    .map(it -> this.electrumClient.delegate().getInfo().getBlockchainHeight())
                    .filter(newBlockchainHeight -> newBlockchainHeight > currentServerHeight)
                    .blockFirst(Duration.ofSeconds(30));
        });
        GetBalanceParams balanceParams = GetBalanceParams.builder()
                .walletPath(walletParams.getWalletPath())
                .build();

        return Mono.from(electrumRegtestActions.awaitWalletSynchronized(walletParams, Duration.ofSeconds(10)))
                .map(it -> electrumClient.getBalance(balanceParams).getSpendable())
                .filter(spendable -> {
                    boolean hasEnoughFunds = !spendable.isLessThan(neededSpendableAmount);
                    log.debug("does the faucet control enough funds? {} (spendable {} less than {} needed)",
                            hasEnoughFunds, spendable.toFriendlyString(), neededSpendableAmount.toFriendlyString());
                    return hasEnoughFunds;
                })
                // mine a new coinbase reward to an address the electrum client is in control of
                .switchIfEmpty(fundWithCoinbaseReward
                        .flatMap(address -> Mono.from(awaitBlockchainHeightIncrease))
                        .map(blockheight -> electrumClient.getBalance(balanceParams).getSpendable()))
                .repeat()
                .takeWhile(spendable -> {
                    boolean mineMoreBlocks = spendable.isLessThan(neededSpendableAmount);
                    log.debug("does the faucet needs more coinbase rewards? {} (spendable {} less than {} needed)",
                            mineMoreBlocks, spendable.toFriendlyString(), neededSpendableAmount.toFriendlyString());
                    return mineMoreBlocks;
                })
                .collectList()
                .flatMap(receivedAmount -> Mono.from(electrumRegtestActions.sendPaymentAndAwaitTx(walletParams, destinationAddress.get(), amount, txFee)))
                .map(OnchainHistory.Transaction::getTxHash)
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
