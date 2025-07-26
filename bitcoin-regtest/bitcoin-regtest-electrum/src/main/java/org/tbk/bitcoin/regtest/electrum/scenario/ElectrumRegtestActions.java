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

    public AwaitSpendableBalanceOnAddressAction awaitBalanceOnAddress(WalletParams wallet, Coin expectedAmount, Address address) {
        return new AwaitSpendableBalanceOnAddressAction(electrumClient, wallet, expectedAmount, address);
    }

    public AwaitExactPaymentAction awaitExactPayment(WalletParams wallet, Coin expectedAmount, Address address) {
        return new AwaitExactPaymentAction(electrumClient, wallet, expectedAmount, address);
    }

    public AwaitSpendableBalanceAction awaitSpendableBalance(WalletParams wallet, Coin expectedAmount) {
        return new AwaitSpendableBalanceAction(electrumClient, wallet, expectedAmount);
    }

    public AwaitTransactionAction awaitTransaction(WalletParams wallet, Sha256Hash txid, int confirmations) {
        return new AwaitTransactionAction(electrumClient.delegate(), wallet, txid, confirmations);
    }

    @Deprecated
    public SendToAddressAction sendPayment(WalletParams wallet, Address address, Coin amount) {
        return new SendToAddressAction(electrumClient, wallet, address, amount);
    }

    public SendToAddressAction sendPayment(WalletParams wallet, Address address, Coin amount, Coin txFee) {
        return new SendToAddressAction(electrumClient, wallet, address, amount, txFee);
    }

    /**
     * Send a payment and wait for the tx to be recognized by electrum.
     * This action usually takes around 5 seconds to complete.
     *
     * @param address the destination address
     * @param amount  the amount sent to address
     * @return the action itself
     */
    public RegtestAction<OnchainHistory.Transaction> sendPaymentAndAwaitTx(WalletParams wallet, Address address, Coin amount) {
        return s -> Mono.from(sendPayment(wallet, address, amount))
                .flatMap(txId -> Mono.from(awaitTransaction(wallet, txId, 0)))
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
    public RegtestAction<OnchainHistory.Transaction> sendPaymentAndAwaitTx(WalletParams wallet, Address address, Coin amount, Coin txFee) {
        return s -> Mono.from(sendPayment(wallet, address, amount, txFee))
                .flatMap(txId -> Mono.from(awaitTransaction(wallet, txId, 0)))
                .subscribe(s);
    }

    public AwaitWalletSynchronizedAction awaitWalletSynchronized(WalletParams wallet, Duration timeout) {
        return new AwaitWalletSynchronizedAction(electrumClient.delegate(), wallet, timeout);
    }
}
