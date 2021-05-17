package org.tbk.bitcoin.regtest.electrum.scenario;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.tbk.electrum.ElectrumClient;

import static java.util.Objects.requireNonNull;

public final class ElectrumRegtestActions {

    private final ElectrumClient electrumClient;

    public ElectrumRegtestActions(ElectrumClient electrumClient) {
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
        return new AwaitTransactionAction(electrumClient, txid, confirmations);
    }

    public SendToAddressAction sendPayment(Address address, Coin amount) {
        return new SendToAddressAction(electrumClient, address, amount);
    }
}
