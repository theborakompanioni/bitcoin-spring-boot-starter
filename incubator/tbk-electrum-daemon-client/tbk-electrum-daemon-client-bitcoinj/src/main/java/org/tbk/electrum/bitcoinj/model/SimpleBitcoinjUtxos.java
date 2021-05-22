package org.tbk.electrum.bitcoinj.model;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.bitcoinj.core.Coin;

import java.util.Collections;
import java.util.List;

@Value
@Builder
public class SimpleBitcoinjUtxos implements BitcoinjUtxos {

    public static BitcoinjUtxos empty() {
        return SimpleBitcoinjUtxos.builder()
                .utxos(Collections.emptyList())
                .build();
    }

    @Singular("addUtxo")
    List<BitcoinjUtxo> utxos;

    @Override
    public List<BitcoinjUtxo> getUtxos() {
        return ImmutableList.copyOf(utxos);
    }

    @Override
    public boolean isEmpty() {
        return utxos.isEmpty();
    }

    @Override
    public Coin getValue() {
        return utxos.stream()
                .map(BitcoinjUtxo::getValue)
                .reduce(Coin.ZERO, Coin::add);
    }
}
