package org.tbk.electrum.bitcoinj;

import org.bitcoinj.core.Coin;
import org.tbk.electrum.bitcoinj.model.*;
import org.tbk.electrum.model.Balance;
import org.tbk.electrum.model.TxoValue;
import org.tbk.electrum.model.Utxo;
import org.tbk.electrum.model.Utxos;

public final class BitcoinjHelper {
    private BitcoinjHelper() {
        throw new UnsupportedOperationException();
    }

    public static Coin toCoin(TxoValue val) {
        return Coin.valueOf(val.getValue());
    }

    public static BitcoinjBalance toBitcoinjBalance(Balance balance) {
        return SimpleBitcoinjBalance.builder()
                .balance(balance)
                .build();
    }

    public static BitcoinjUtxo toBitcoinjUtxo(Utxo utxo) {
        return SimpleBitcoinjUtxo.builder()
                .utxo(utxo)
                .build();
    }

    public static BitcoinjUtxos toBitcoinjUtxos(Utxos utxos) {
        return SimpleBitcoinjUtxos.builder()
                .utxos(utxos.getUtxos().stream()
                        .map(BitcoinjHelper::toBitcoinjUtxo)
                        .toList())
                .build();
    }
}
