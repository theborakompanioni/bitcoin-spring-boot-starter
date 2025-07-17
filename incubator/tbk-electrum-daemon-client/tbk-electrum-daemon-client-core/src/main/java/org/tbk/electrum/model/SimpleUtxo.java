package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.tbk.electrum.rpc.command.AddressUnspentResponse;
import org.tbk.electrum.rpc.command.ListUnspentResponse;

@Value
@Builder
public class SimpleUtxo implements Utxo {
    public static SimpleUtxo from(AddressUnspentResponse.AddressUnspentEntry val) {
        return SimpleUtxo.builder()
                .height(val.getHeight())
                .txHash(val.getTxHash())
                .txPos(val.getTxPos())
                .value(SimpleTxoValue.of(val.getValue()))
                .build();
    }
    public static SimpleUtxo from(ListUnspentResponse.ListUnspentEntry val) {
        return SimpleUtxo.builder()
                .height(val.getHeight())
                .txHash(val.getPrevoutHash())
                .txPos(val.getPrevoutN())
                .value(BtcTxoValues.fromBtcString(val.getValue()))
                .build();
    }



    long height;

    @NonNull
    String txHash;

    int txPos;

    @NonNull
    TxoValue value;
}
