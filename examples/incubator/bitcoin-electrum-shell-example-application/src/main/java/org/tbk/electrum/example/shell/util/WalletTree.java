package org.tbk.electrum.example.shell.util;

import fr.acinq.bitcoin.KeyPath;
import reactor.core.publisher.Flux;

public final class WalletTree {
    private WalletTree() {
        throw new UnsupportedOperationException();
    }

    public static Flux<Wallet.AddressAndPath> tree(Wallet wallet, int amount) {
        return Flux.merge(
                wallet.p2pkh(new KeyPath("")).take(amount),
                wallet.p2pkh(Wallet.deprecatedBip32P2pkhPath().derive(0)).take(amount),
                wallet.p2pkh(Wallet.deprecatedBip32P2pkhPath().derive(1)).take(amount),
                wallet.p2pkh(0, 0).take(amount),
                wallet.p2pkh(0, 1).take(amount),
                wallet.p2pkh(1, 0).take(amount),
                wallet.p2pkh(1, 1).take(amount),
                wallet.p2sh(0, 0).take(amount),
                wallet.p2sh(0, 1).take(amount),
                wallet.p2sh(1, 0).take(amount),
                wallet.p2sh(1, 1).take(amount),
                wallet.p2wpkh(0, 0).take(amount),
                wallet.p2wpkh(0, 1).take(amount),
                wallet.p2wpkh(1, 0).take(amount),
                wallet.p2wpkh(1, 1).take(amount),
                wallet.p2tr(0, 0).take(amount),
                wallet.p2tr(0, 1).take(amount),
                wallet.p2tr(1, 0).take(amount),
                wallet.p2tr(1, 1).take(amount)
        );
    }

}
