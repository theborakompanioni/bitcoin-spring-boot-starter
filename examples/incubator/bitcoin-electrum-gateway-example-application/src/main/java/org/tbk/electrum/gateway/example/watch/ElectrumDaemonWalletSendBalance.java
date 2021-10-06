package org.tbk.electrum.gateway.example.watch;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Coin;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.History;
import org.tbk.electrum.model.RawTx;
import org.tbk.electrum.model.TxoValue;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

@Slf4j
public class ElectrumDaemonWalletSendBalance implements Callable<Boolean> {
    @Value
    @Builder
    public static class Options {
        /**
         * @param walletPassphrase the wallet passphrase
         * @return the wallet passphrase
         */
        @Nullable
        String walletPassphrase;

        /**
         * @param destinationAddress the destination address
         * @return the destination address
         */
        @NonNull
        String destinationAddress;
    }

    private final ElectrumClient client;
    private final Options options;

    private final AtomicReference<TxoValue> incoming = new AtomicReference<>();

    public ElectrumDaemonWalletSendBalance(ElectrumClient client, Options options) {
        this.client = requireNonNull(client);
        this.options = requireNonNull(options);
    }

    @Override
    public Boolean call() {
        try {
            return callInner();
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    private Boolean callInner() {
        History history = client.getHistory();

        History.Summary summary = history.getSummary();

        log.debug("{}", summary);

        TxoValue currentIncoming = summary.getIncoming();
        TxoValue previousIncoming = this.incoming.getAndSet(currentIncoming);
        if (previousIncoming == null) {
            return false;
        }

        boolean incomingValueStayedTheSame = previousIncoming.equals(currentIncoming);
        if (incomingValueStayedTheSame) {
            return false;
        }

        log.info("found end balance: {}", Coin.valueOf(summary.getEndBalance().getValue()).toFriendlyString());

        RawTx unsignedTransaction = client
                .createUnsignedTransactionSendingEntireBalance(options.getDestinationAddress());

        RawTx rawTx = client.signTransaction(unsignedTransaction, options.getWalletPassphrase());

        log.info("rawTx (signed): {}", rawTx);

        String broadcast = client.broadcast(rawTx);

        log.info("broadcast: {}", broadcast);

        return true;
    }
}