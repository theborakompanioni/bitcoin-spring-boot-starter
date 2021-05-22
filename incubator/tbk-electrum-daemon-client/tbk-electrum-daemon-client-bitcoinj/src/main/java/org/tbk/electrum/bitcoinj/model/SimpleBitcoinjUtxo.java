package org.tbk.electrum.bitcoinj.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.tbk.electrum.model.Utxo;

import static org.tbk.electrum.bitcoinj.BitcoinjHelper.toCoin;

@Value
@Builder
public class SimpleBitcoinjUtxo implements BitcoinjUtxo {

    @NonNull
    Utxo utxo;

    @Override
    public Sha256Hash getTxHash() {
        return Sha256Hash.wrap(utxo.getTxHash());
    }

    @Override
    public int getTxPos() {
        return utxo.getTxPos();
    }

    @Override
    public long getHeight() {
        return utxo.getHeight();
    }

    @Override
    public Coin getValue() {
        return toCoin(utxo.getValue());
    }
}
