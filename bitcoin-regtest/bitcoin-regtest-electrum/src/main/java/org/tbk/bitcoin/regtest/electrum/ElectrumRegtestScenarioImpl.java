package org.tbk.bitcoin.regtest.electrum;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.params.RegTestParams;
import org.jetbrains.annotations.NotNull;
import org.tbk.bitcoin.regtest.mining.BitcoindRegtestMiner;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.Balance;
import org.tbk.electrum.model.History;
import org.tbk.electrum.model.RawTx;
import org.tbk.electrum.model.SimpleTxoValue;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
public class ElectrumRegtestScenarioImpl implements ElectrumRegtestScenario {

    private static final Coin defaultTxFee = Coin.valueOf(200_000);

    private final ElectrumClient client;
    private final BitcoindRegtestMiner miner;

    public ElectrumRegtestScenarioImpl(BitcoindRegtestMiner miner, ElectrumClient client) {
        this.miner = miner;
        this.client = client;
    }

    public ElectrumRegtestScenarioImpl sendToAddress(Address address, Coin coin) {
        sendToAddress(address, coin, defaultTxFee, 0);
        return this;
    }

    public ElectrumRegtestScenarioImpl sendToAddress(Address address, Coin amount, Coin txFee, int confirmations) {
        checkArgument(amount.isPositive(), "'amount' must be positive");
        checkArgument(txFee.isPositive(), "'txFee' must be positive");

        boolean acceptableAmount = amount.isLessThan(Coin.FIFTY_COINS.minus(txFee));
        if (!acceptableAmount) {
            String errorMessage = String.format("Cannot send more than %s in this scenario", Coin.FIFTY_COINS);
            throw new IllegalArgumentException(errorMessage);
        }

        ElectrumRegtestFundingSource fundingSource = new ElectrumRegtestFundingSource(client);

        Coin expectedAmount = amount.add(txFee);
        Supplier<Boolean> hasEnoughFunds = () -> !fundingSource.getSpendableBalance().isLessThan(expectedAmount);

        Address newAddress = fundingSource.getNewAddress();
        if (!hasEnoughFunds.get()) {
            log.debug("Will mine 1 block with coinbase on address {}", newAddress);
            miner.mineBlocks(1, () -> newAddress);

            log.debug("Will mine 100 blocks to mature funds on {}", newAddress);
            miner.mineBlocks(100); // make coins spendable

            log.debug("Waiting at most 30s for coinbase reward on {} to be spendable by electrum.. ", newAddress);
            Coin spendableCoinbase = waitForMinimumSpendableAmount(fundingSource, expectedAmount)
                    .blockFirst(Duration.ofSeconds(30));

            requireNonNull(spendableCoinbase, "could not fund electrum with enough balance in time");

            log.debug("Electrum funding source now in control of {}.. ", spendableCoinbase.toFriendlyString());
        }

        // recheck that we have mined enough funds if necessary
        if (!hasEnoughFunds.get()) {
            String errorMessage = String.format("Could not mine enough funds to send %s coins to %s", amount, address);
            throw new IllegalStateException(errorMessage);
        }

        log.debug("Will send {} to {}", amount, address);
        RawTx unsignedTransaction = client.createUnsignedTransaction(SimpleTxoValue.of(amount.getValue()),
                address.toString(), newAddress.toString(), SimpleTxoValue.of(txFee.getValue()));
        RawTx rawTx = client.signTransaction(unsignedTransaction, null);
        String broadcastedTxid = client.broadcast(rawTx);

        log.debug("Will mine {} blocks to confirm tx {}", confirmations, broadcastedTxid);
        if (confirmations > 0) {
            miner.mineBlocks(confirmations);
        }
        log.debug("Waiting at most 30s for tx {} to be processed by electrum.. ", broadcastedTxid);
        History.Transaction broadcastedTx = waitForTransactionIsProcessedByElectrum(broadcastedTxid, confirmations)
                .blockFirst(Duration.ofSeconds(30));

        requireNonNull(broadcastedTx, "electrum could not processes transaction in time");

        return this;
    }

    @NotNull
    private Flux<Coin> waitForMinimumSpendableAmount(ElectrumRegtestFundingSource fundingSource, Coin expectedAmount) {
        return Flux.interval(Duration.ofMillis(30))
                .doOnNext(it -> log.trace("Waiting for coinbase reward to be spendable by electrum.. ({} attempt)", it))
                .map(it -> fundingSource.getSpendableBalance())
                .filter(it -> !it.isLessThan(expectedAmount));
    }

    private Flux<History.Transaction> waitForTransactionIsProcessedByElectrum(String txid, int confirmations) {
        return waitForTransactionIsProcessedByElectrum(Duration.ofMillis(10), txid, confirmations);
    }

    /**
     * immediately after the block is mined, the electrum client sometimes
     * reports the balance as zero for a short amount of time..
     * solution: poll every 10ms for 10s as a short workaround
     */
    private Flux<History.Transaction> waitForTransactionIsProcessedByElectrum(Duration checkInterval, String txid, int confirmations) {
        return Flux.interval(checkInterval)
                .doOnNext(it -> log.trace("Waiting for tx {} to be processed by electrum.. ({} attempt)", txid, it))
                .flatMapIterable(it -> this.client.getHistory().getTransactions())
                .filter(it -> txid.equals(it.getTxHash()))
                .filter(it -> confirmations <= it.getConfirmations());
    }

    public interface RegtestFundingSource {
        Address getNewAddress();

        Coin getSpendableBalance();
    }

    public static class ElectrumRegtestFundingSource implements RegtestFundingSource {

        private final ElectrumClient electrumClient;

        public ElectrumRegtestFundingSource(ElectrumClient electrumClient) {
            // TODO: require network to be "regtest"
            this.electrumClient = electrumClient;
        }

        @Override
        public Address getNewAddress() {
            String address = this.electrumClient.getUnusedAddress()
                    .orElseGet(this.electrumClient::createNewAddress);

            return Address.fromString(RegTestParams.get(), address);
        }

        @Override
        public Coin getSpendableBalance() {
            Balance balance = this.electrumClient.getBalance();
            return Coin.valueOf(balance.getSpendable().getValue());
        }
    }
}
