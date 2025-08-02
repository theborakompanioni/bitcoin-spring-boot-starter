package org.tbk.bitcoin.regtest.electrum.faucet;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.params.RegTestParams;
import org.tbk.bitcoin.regtest.common.AddressSupplier;
import org.tbk.bitcoin.regtest.electrum.scenario.ElectrumRegtestActions;
import org.tbk.bitcoin.regtest.scenario.BitcoinRegtestActions;
import org.tbk.electrum.bitcoinj.BitcoinjElectrumClient;
import org.tbk.electrum.common.WalletParams;
import org.tbk.electrum.model.OnchainHistory;
import org.tbk.electrum.model.SimpleTxoValue;
import org.tbk.electrum.rpc.command.AddRequestParams;
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
    private static final Coin minAllowedAmountPerRequest = Coin.SATOSHI.multiply(1_000);
    private static final Coin maxAllowedAmountPerRequest = Coin.COIN.multiply(100);
    private static final Coin txFee = Coin.valueOf(50_000L);
    // apply a default timeout as sending from the faucet should not really take too long
    private static final Duration defaultRequestBitcoinTimeout = Duration.ofMinutes(3);

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

        Coin neededAmount = amount.plus(txFee);

        Mono<Address> rewardAddress = Mono.fromCallable(() -> electrumClient.delegate().addRequest(AddRequestParams.builder()
                        .walletPath(walletParams.getWalletPath())
                        .amount(SimpleTxoValue.zero())
                        .expiry(Duration.ZERO)
                        .build()))
                .map(it -> Address.fromString(RegTestParams.get(), it.getAddress()))
                .cache();

        Mono<Address> fundWithCoinbaseReward = Mono.from(rewardAddress)
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

        return Mono.from(electrumRegtestActions.awaitWalletSynchronized(walletParams, Duration.ofSeconds(30)))
                .map(it -> electrumClient.getBalance(balanceParams))
                .filter(balance -> balance.getTotalOnChain().isPositive())
                .switchIfEmpty(Mono.just(1)
                        .doOnNext(it -> log.debug("Faucet is empty, initialize by funding with coinbase reward."))
                        .then(fundWithCoinbaseReward)
                        .flatMap(address -> Mono.from(awaitBlockchainHeightIncrease))
                        .flatMap(it -> Mono.from(electrumRegtestActions.awaitWalletSynchronized(walletParams, Duration.ofSeconds(30))))
                        .map(it -> electrumClient.getBalance(balanceParams)))
                .filter(balance -> !balance.getConfirmed().isLessThan(neededAmount))
                .switchIfEmpty(Mono.from(rewardAddress)
                        .doOnNext(it -> log.debug("Faucet has funds but needs more blocks for them to be confirmed."))
                        .flatMap(address -> Mono.from(bitcoinRegtestActions.mineBlockWithCoinbase(() -> address, 1)))
                        .flatMap(it -> Mono.from(awaitBlockchainHeightIncrease))
                        .map(it -> electrumClient.getBalance(balanceParams)))
                .repeat()
                .takeWhile(balance -> {
                    boolean hasEnoughFunds = !balance.getConfirmed().isLessThan(neededAmount);
                    log.debug("Does the faucet control enough funds? {} (needed {}; confirmed {}; unconfirmed {}; unmatured {};)",
                            hasEnoughFunds, neededAmount.toFriendlyString(),
                            balance.getConfirmed().toFriendlyString(),
                            balance.getUnconfirmed().toFriendlyString(),
                            balance.getUnmatured().toFriendlyString());
                    return !hasEnoughFunds;
                })
                .collectList()
                .then(Mono.from(electrumRegtestActions.sendPaymentAndAwaitTx(walletParams, destinationAddress.get(), amount, txFee)))
                .map(OnchainHistory.Transaction::getTxHash)
                .map(Sha256Hash::wrap)
                .timeout(defaultRequestBitcoinTimeout);
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
