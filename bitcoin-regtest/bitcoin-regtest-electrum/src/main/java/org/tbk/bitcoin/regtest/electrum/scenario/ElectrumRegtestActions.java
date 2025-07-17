package org.tbk.bitcoin.regtest.electrum.scenario;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.tbk.electrum.common.WalletParams;
import org.tbk.bitcoin.regtest.scenario.RegtestAction;
import org.tbk.electrum.bitcoinj.BitcoinjElectrumClient;
import org.tbk.electrum.model.OnchainHistory;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static java.util.Objects.requireNonNull;

@Slf4j
public final class ElectrumRegtestActions {

    private final BitcoinjElectrumClient electrumClient;

    public ElectrumRegtestActions(BitcoinjElectrumClient electrumClient) {
        this.electrumClient = requireNonNull(electrumClient);
    }

    public AwaitSpendableBalanceOnAddressAction awaitBalanceOnAddress(Coin expectedAmount, Address address) {
        return new AwaitSpendableBalanceOnAddressAction(electrumClient, expectedAmount, address);
    }

    public AwaitExactPaymentAction awaitExactPayment(Coin expectedAmount, Address address) {
        return new AwaitExactPaymentAction(electrumClient, expectedAmount, address);
    }

    public AwaitSpendableBalanceAction awaitSpendableBalance(Coin expectedAmount) {
        return new AwaitSpendableBalanceAction(electrumClient, expectedAmount);
    }

    public AwaitTransactionAction awaitTransaction(WalletParams params, Sha256Hash txid, int confirmations) {
        return new AwaitTransactionAction(electrumClient.delegate(), params, txid, confirmations);
    }

    @Deprecated
    public SendToAddressAction sendPayment(WalletParams params, Address address, Coin amount) {
        return new SendToAddressAction(electrumClient, params, address, amount);
    }

    public SendToAddressAction sendPayment(WalletParams params, Address address, Coin amount, Coin txFee) {
        return new SendToAddressAction(electrumClient, params, address, amount, txFee);
    }

    /**
     * Send a payment and wait for the tx to be recognized by electrum.
     * This action usually takes around 5 seconds to complete.
     *
     * @param address the destination address
     * @param amount  the amount sent to address
     * @return the action itself
     */
    public RegtestAction<OnchainHistory.Transaction> sendPaymentAndAwaitTx(WalletParams params, Address address, Coin amount) {
        return s -> Mono.from(sendPayment(params, address, amount))
                .flatMap(txId -> Mono.from(awaitTransaction(params, txId, 0)))
                .subscribe(s);
    }

    /**
     * Send a payment and wait for the tx to be recognized by electrum.
     * This action usually takes around 5 seconds to complete.
     *
     * @param address the destination address
     * @param amount  the amount sent to address
     * @param txFee   the transaction fee
     * @return the action itself
     */
    public RegtestAction<OnchainHistory.Transaction> sendPaymentAndAwaitTx(WalletParams params, Address address, Coin amount, Coin txFee) {
        return s -> Mono.from(sendPayment(params, address, amount, txFee))
                .flatMap(txId -> Mono.from(awaitTransaction(params, txId, 0)))
                .subscribe(s);
    }

    public AwaitWalletSynchronizedAction awaitWalletSynchronized(WalletParams params, Duration timeout) {
        return new AwaitWalletSynchronizedAction(electrumClient.delegate(), params, timeout);
    }
}
