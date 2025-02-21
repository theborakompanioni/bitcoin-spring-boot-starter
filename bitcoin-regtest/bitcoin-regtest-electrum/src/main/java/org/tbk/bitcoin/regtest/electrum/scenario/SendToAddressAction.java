package org.tbk.bitcoin.regtest.electrum.scenario;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.reactivestreams.Subscriber;
import org.tbk.bitcoin.regtest.scenario.RegtestAction;
import org.tbk.electrum.bitcoinj.BitcoinjElectrumClient;
import org.tbk.electrum.bitcoinj.model.BitcoinjBalance;
import org.tbk.electrum.model.OnchainHistory;
import org.tbk.electrum.model.RawTx;
import org.tbk.electrum.model.SimpleTxoValue;
import org.tbk.electrum.model.TxoValue;
import reactor.core.publisher.Mono;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
public final class SendToAddressAction implements RegtestAction<Sha256Hash> {

    private static final Coin defaultTxFee = Coin.valueOf(200_000);

    private final BitcoinjElectrumClient client;
    private final Address address;
    private final Coin amount;
    private final Coin txFee;

    public SendToAddressAction(BitcoinjElectrumClient client, Address address, Coin amount) {
        this(client, address, amount, defaultTxFee);
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "false positive")
    public SendToAddressAction(BitcoinjElectrumClient client, Address address, Coin amount, Coin txFee) {
        this.client = requireNonNull(client);
        this.address = requireNonNull(address);
        this.amount = requireNonNull(amount);
        this.txFee = requireNonNull(txFee);

        checkArgument(amount.isPositive(), "'amount' must be positive");
        checkArgument(txFee.isPositive(), "'txFee' must be positive");
    }

    @Override
    public void subscribe(Subscriber<? super Sha256Hash> s) {
        create().subscribe(s);
    }

    private Mono<Sha256Hash> create() {
        return Mono.fromCallable(() -> {
            Address changeAddress = client.createNewAddress();

            log.debug("Will try to send {} to address {} (with change to {})", amount.toFriendlyString(), address, changeAddress);

            if (log.isTraceEnabled()) {
                BitcoinjBalance balance = client.getBalance();

                log.trace("Balance: {} total", balance.getTotal().toFriendlyString());
                log.trace("         {} confirmed", balance.getConfirmed().toFriendlyString());
                log.trace("         {} unconfirmed", balance.getUnconfirmed().toFriendlyString());
                log.trace("         {} spendable", balance.getSpendable().toFriendlyString());
                log.trace("         {} unmatured", balance.getUnmatured().toFriendlyString());

                OnchainHistory history = client.delegate().getOnchainHistory();
                OnchainHistory.Summary summary = history.getSummary();

                log.trace("History: {} end balance", friendlyBtcString(summary.getEndBalance()));
                log.trace("         {} start balance", friendlyBtcString(summary.getStartBalance()));
                log.trace("         {} outgoing", friendlyBtcString(summary.getIncoming()));
                log.trace("         {} incoming", friendlyBtcString(summary.getOutgoing()));
            }

            RawTx unsignedTransaction = client.delegate().createUnsignedTransaction(
                    SimpleTxoValue.of(amount.getValue()),
                    address.toString(),
                    changeAddress.toString(),
                    SimpleTxoValue.of(txFee.getValue())
            );

            RawTx rawTx = client.delegate().signTransaction(unsignedTransaction, null);

            String broadcastTxid = client.delegate().broadcast(rawTx);

            log.debug("Broadcast tx {} with electrum.. ", broadcastTxid);

            return Sha256Hash.wrap(broadcastTxid);
        });
    }

    private static String friendlyBtcString(TxoValue txoValue) {
        return Coin.valueOf(txoValue.getValue()).toFriendlyString();
    }
}
