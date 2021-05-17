package org.tbk.bitcoin.regtest.electrum.scenario;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.reactivestreams.Subscriber;
import org.tbk.bitcoin.regtest.scenario.RegtestAction;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.*;
import reactor.core.publisher.Flux;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
public final class SendToAddressAction implements RegtestAction<Sha256Hash> {

    private static final Coin defaultTxFee = Coin.valueOf(200_000);

    private final ElectrumClient client;
    private final Address address;
    private final Coin amount;
    private final Coin txFee;

    public SendToAddressAction(ElectrumClient client, Address address, Coin amount) {
        this(client, address, amount, defaultTxFee);
    }

    public SendToAddressAction(ElectrumClient client, Address address, Coin amount, Coin txFee) {
        this.client = requireNonNull(client);
        this.address = requireNonNull(address);
        this.amount = requireNonNull(amount);
        this.txFee = requireNonNull(txFee);

        checkArgument(amount.isPositive(), "'amount' must be positive");
        checkArgument(txFee.isPositive(), "'txFee' must be positive");

        // move to fund and send
        boolean acceptableAmount = amount.isLessThan(Coin.FIFTY_COINS.minus(txFee));
        if (!acceptableAmount) {
            String errorMessage = String.format("Cannot send more than %s with this action", Coin.FIFTY_COINS.toFriendlyString());
            throw new IllegalArgumentException(errorMessage);
        }
    }

    @Override
    public void subscribe(Subscriber<? super Sha256Hash> s) {
        create().subscribe(s);
    }

    private Flux<Sha256Hash> create() {
        return Flux.defer(() -> {
            String changeAddress = client.createNewAddress();

            log.debug("Will try to send {} to address {} (with change to {})", amount.toFriendlyString(), address, changeAddress);

            Balance balance = client.getBalance();

            log.trace("Balance: {} total", friendlyBtcString(balance.getTotal()));
            log.trace("         {} confirmed", friendlyBtcString(balance.getConfirmed()));
            log.trace("         {} unconfirmed", friendlyBtcString(balance.getUnconfirmed()));
            log.trace("         {} spendable", friendlyBtcString(balance.getSpendable()));
            log.trace("         {} unmatured", friendlyBtcString(balance.getUnmatured()));

            History history = client.getHistory();
            History.Summary summary = history.getSummary();

            log.trace("History: {} end balance", friendlyBtcString(summary.getEndBalance()));
            log.trace("         {} start balance", friendlyBtcString(summary.getStartBalance()));
            log.trace("         {} outgoing", friendlyBtcString(summary.getIncoming()));
            log.trace("         {} incoming", friendlyBtcString(summary.getOutgoing()));

            RawTx unsignedTransaction = client.createUnsignedTransaction(SimpleTxoValue.of(amount.getValue()),
                    address.toString(), changeAddress, SimpleTxoValue.of(txFee.getValue()));

            RawTx rawTx = client.signTransaction(unsignedTransaction, null);

            String broadcastTxid = client.broadcast(rawTx);

            log.debug("Broadcast tx {} with electrum.. ", broadcastTxid);

            return Flux.just(Sha256Hash.wrap(broadcastTxid));
        });
    }

    private static String friendlyBtcString(TxoValue txoValue) {
        return Coin.valueOf(txoValue.getValue()).toFriendlyString();
    }
}
