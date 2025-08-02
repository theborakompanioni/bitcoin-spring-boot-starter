package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.tbk.electrum.rpc.command.AddressUnspentResponse;
import org.tbk.electrum.rpc.command.ListUnspentResponse;

import javax.annotation.Nullable;
import java.util.Optional;

@Value
@Builder
public class SimpleUtxo implements Utxo {
    public static SimpleUtxo from(String address, AddressUnspentResponse.AddressUnspentEntry val) {
        return SimpleUtxo.builder()
                .height(val.getHeight())
                .txHash(val.getTxHash())
                .txPos(val.getTxPos())
                .value(SimpleTxoValue.of(val.getValue()))
                .address(address)
                .build();
    }

    public static SimpleUtxo from(ListUnspentResponse.ListUnspentEntry val) {
        return SimpleUtxo.builder()
                .height(val.getHeight())
                .txHash(val.getPrevoutHash())
                .txPos(val.getPrevoutN())
                .value(BtcTxoValues.fromBtcString(val.getValue()))
                .address(val.getAddress())
                .build();
    }

    long height;

    @NonNull
    String txHash;

    int txPos;

    @NonNull
    TxoValue value;

    @Nullable
    String address;

    @Override
    public Optional<String> getAddress() {
        return Optional.ofNullable(address);
    }
}
