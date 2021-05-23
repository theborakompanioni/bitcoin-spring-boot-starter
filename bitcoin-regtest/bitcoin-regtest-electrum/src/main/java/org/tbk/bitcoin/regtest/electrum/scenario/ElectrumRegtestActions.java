package org.tbk.bitcoin.regtest.electrum.scenario;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.tbk.bitcoin.regtest.scenario.RegtestAction;
import org.tbk.electrum.bitcoinj.BitcoinjElectrumClient;
import org.tbk.electrum.model.History;
import reactor.core.publisher.Mono;

import static java.util.Objects.requireNonNull;

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

    public AwaitTransactionAction awaitTransaction(Sha256Hash txid, int confirmations) {
        return new AwaitTransactionAction(electrumClient.delegate(), txid, confirmations);
    }

    public SendToAddressAction sendPayment(Address address, Coin amount) {
        return new SendToAddressAction(electrumClient, address, amount);
    }

    public SendToAddressAction sendPayment(Address address, Coin amount, Coin txFee) {
        return new SendToAddressAction(electrumClient, address, amount, txFee);
    }

    /**
     * Send a payment and wait for the tx to be recognized by electrum.
     * This action usually takes around 5 seconds to complete.
     *
     * @param address the destination address
     * @param amount the amount sent to address
     * @return the action itself
     */
    public RegtestAction<History.Transaction> sendPaymentAndAwaitTx(Address address, Coin amount) {
        return s -> Mono.from(sendPayment(address, amount))
                .flatMap(txId -> Mono.from(awaitTransaction(txId, 0)))
                .subscribe(s);
    }

    /**
     * Send a payment and wait for the tx to be recognized by electrum.
     * This action usually takes around 5 seconds to complete.
     *
     * @param address the destination address
     * @param amount the amount sent to address
     * @param txFee the transaction fee
     * @return the action itself
     */
    public RegtestAction<History.Transaction> sendPaymentAndAwaitTx(Address address, Coin amount, Coin txFee) {
        return s -> Mono.from(sendPayment(address, amount, txFee))
                .flatMap(txId -> Mono.from(awaitTransaction(txId, 0)))
                .subscribe(s);
    }
}
