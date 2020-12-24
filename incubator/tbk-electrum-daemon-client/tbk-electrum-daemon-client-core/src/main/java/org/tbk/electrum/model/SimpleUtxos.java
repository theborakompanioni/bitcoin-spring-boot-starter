package org.tbk.electrum.model;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Collections;
import java.util.List;

@Value
@Builder
public class SimpleUtxos implements Utxos {

    public static Utxos empty() {
        return SimpleUtxos.builder()
                .utxos(Collections.emptyList())
                .build();
    }

    @Singular("addUtxo")
    List<Utxo> utxos;

    @Override
    public List<Utxo> getUtxos() {
        return ImmutableList.copyOf(utxos);
    }

    @Override
    public boolean isEmpty() {
        return utxos.isEmpty();
    }

    @Override
    public TxoValue getValue() {
        return SimpleTxoValue.of(utxos.stream()
                .map(Utxo::getValue)
                .mapToLong(TxoValue::getValue)
                .sum());
    }
}
