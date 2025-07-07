package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.tbk.electrum.command.AddressUnspentResponse;

@Value
@Builder
public class SimpleUtxo implements Utxo {
    public static SimpleUtxo from(AddressUnspentResponse.Utxo val) {
        return SimpleUtxo.builder()
                .height(val.getHeight())
                .txHash(val.getTxHash())
                .txPos(val.getTxPos())
                .value(SimpleTxoValue.of(val.getValue()))
                .build();
    }

    long height;

    @NonNull
    String txHash;

    int txPos;

    @NonNull
    TxoValue value;
}
