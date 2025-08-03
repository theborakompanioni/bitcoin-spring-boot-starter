package org.tbk.bitcoin.regtest.electrum.faucet;

import com.github.arteam.simplejsonrpc.client.exception.JsonRpcException;
import com.google.common.base.Throwables;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.params.RegTestParams;
import org.reactivestreams.Publisher;
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
import reactor.util.retry.Retry;

import java.time.Duration;

import static java.util.Objects.requireNonNull;

@Slf4j
public class SimpleElectrumRegtestFaucet implements ElectrumRegtestFaucet {
    // e.g. below electrum will throw "TxBroadcastServerReturnedError: Transaction could not be broadcast due to dust outputs."
    private static final Coin minAllowedAmountPerRequest = Coin.SATOSHI.multiply(1_000);
    private static final Coin maxAllowedAmountPerRequest = Coin.COIN.multiply(100);
    private static final Coin txFee = Coin.valueOf(50_000L);
    // apply a default timeout as sending from the faucet should not really take too long
    private static final Duration defaultRequestBitcoinTimeout = Duration.ofSeconds(180);

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

        return Mono.defer(() -> Mono.from(electrumRegtestActions.awaitWalletSynchronized(walletParams, Duration.ofSeconds(30))))
                .map(it -> electrumClient.getBalance(balanceParams))
                .filter(balance -> balance.getTotalOnChain().isPositive())
                .switchIfEmpty(Mono.defer(() -> Mono.just(1))
                        .doOnNext(it -> log.debug("Faucet is empty, initialize by funding with coinbase reward."))
                        .then(fundWithCoinbaseReward)
                        .flatMap(it -> Mono.from(awaitBlockchainHeightIncrease))
                        .flatMap(it -> Mono.from(electrumRegtestActions.awaitWalletSynchronized(walletParams, Duration.ofSeconds(30))))
                        .map(it -> electrumClient.getBalance(balanceParams)))
                .filter(balance -> !balance.getConfirmed().isLessThan(neededAmount))
                .switchIfEmpty(Mono.defer(() -> Mono.from(rewardAddress))
                        .doOnNext(it -> log.debug("Faucet has funds but needs more blocks for them to be confirmed."))
                        .flatMap(address -> Mono.from(bitcoinRegtestActions.mineBlockWithCoinbase(() -> address, 1)))
                        .flatMap(it -> Mono.from(awaitBlockchainHeightIncrease))
                        .flatMap(it -> Mono.from(electrumRegtestActions.awaitWalletSynchronized(walletParams, Duration.ofSeconds(30))))
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
                .retryWhen(requestBitcoinRetryHandler())
                .map(OnchainHistory.Transaction::getTxHash)
                .map(Sha256Hash::wrap)
                .timeout(defaultRequestBitcoinTimeout);
    }
    /*
     * If the wallet only holds one utxo, the electrum daemon can only spend confirmed coins (outside the control of this class),
     * and multiple payments have been started during the same block height, the flow can be retried.
     * This can potentially happen because electrum might be out of sync (even if wallet is signaled as synchronized).
     * - "TxBroadcastServerReturnedError('bad-txns-inputs-missingorspent\\nYou might have a local transaction in your wallet that this transaction builds on top. You need to either broadcast or remove the local tx.')"
     * - "TxBroadcastServerReturnedError('insufficient fee\\nYour transaction is trying to replace another one in the mempool but it does not meet the rules to do so. Try to increase the fee.')"
     * - "NotEnoughFunds()"
     */
    private static Retry requestBitcoinRetryHandler() {
        return Retry.from(retrySignalFlux -> retrySignalFlux
                .doOnNext(it -> log.debug("An error occurred and the action might need to be retried.", it.failure()))
                .filter(it -> it.totalRetries() <= 2)
                .map(Retry.RetrySignal::failure)
                .map(Throwables::getRootCause)
                .filter(it -> JsonRpcException.class.isAssignableFrom(it.getClass()))
                .cast(JsonRpcException.class)
                .map(JsonRpcException::getErrorMessage)
                .filter(it -> it.getCode() == 2)
                .filter(it -> it.getMessage().toLowerCase().contains("internal error while executing rpc"))
                .filter(it -> it.getData() != null && it.getData().get("exception") != null)
                .map(it -> requireNonNull(it.getData()).get("exception").asText("<empty>"))

                .filter(it -> it.startsWith("TxBroadcastServerReturnedError") || it.startsWith("NotEnoughFunds"))
                .doOnNext(it -> log.info("There has been an error and the action will be retried after a delay: '{}'", it))
                .delayElements(Duration.ofSeconds(5))
        );
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
