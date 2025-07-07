package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.tbk.electrum.command.AddressHistoryResponse;

@Value
@Builder
public class SimpleTxHashAndBlockHeight implements TxHashAndBlockHeight {
    public static SimpleTxHashAndBlockHeight from(AddressHistoryResponse.Entry val) {
        return SimpleTxHashAndBlockHeight.builder()
                .height(val.getHeight())
                .txHash(val.getTxHash())
                .build();
    }

    @NonNull
    String txHash;

    long height;

}
