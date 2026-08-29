package org.tbk.electrum.gateway.example.watch;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.base.Coin;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.common.WalletParams;
import org.tbk.electrum.model.OnchainSummary;
import org.tbk.electrum.model.RawTx;
import org.tbk.electrum.model.TxoValue;
import org.tbk.electrum.rpc.command.OnchainCapitalGainsParams;
import org.tbk.electrum.rpc.command.PaytoParams;
import org.tbk.electrum.rpc.command.SignTransactionParams;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

@Slf4j
public class ElectrumDaemonWalletSendBalance implements Callable<Boolean> {
    @Value
    @Builder
    public static class Options {
        @NonNull
        WalletParams walletParams;

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
            log.error("Error during callInner", e);
            return false;
        }
    }

    private Boolean callInner() {
        WalletParams walletParams = options.getWalletParams();

        log.debug("Run ElectrumDaemonWalletSendBalance for {}..", walletParams.getWalletPath());

        OnchainSummary summary = client.getOnchainCapitalGains(OnchainCapitalGainsParams.builder()
                .walletPath(walletParams.getWalletPath())
                .build());

        log.debug("Wallet summary: {}", summary);

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

        RawTx unsignedTransaction = client.createTransaction(PaytoParams.builder()
                .destination(options.getDestinationAddress())
                .amount("!")
                .unsigned(true)
                .walletPath(walletParams.getWalletPath())
                .password(walletParams.getPassword().orElse(null))
                .build());

        RawTx rawTx = client.signTransaction(SignTransactionParams.of(unsignedTransaction)
                .walletPath(walletParams.getWalletPath())
                .password(walletParams.getPassword().orElse(null))
                .build());

        log.info("rawTx (signed): {}", rawTx);

        String broadcast = client.broadcast(rawTx);

        log.info("broadcast: {}", broadcast);

        return true;
    }
}
