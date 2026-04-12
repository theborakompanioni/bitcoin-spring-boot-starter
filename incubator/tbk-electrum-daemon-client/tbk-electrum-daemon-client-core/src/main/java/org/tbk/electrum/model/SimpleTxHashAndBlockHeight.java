package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.tbk.electrum.rpc.command.AddressHistoryResponse;

import java.util.Optional;

@Value
@Builder
public class SimpleTxHashAndBlockHeight implements TxHashAndBlockHeight {
    public static SimpleTxHashAndBlockHeight from(AddressHistoryResponse.Entry val) {
        long height = val.getHeight();
        return SimpleTxHashAndBlockHeight.builder()
                .txHash(val.getTxHash())
                .height(height > 0L ? height : null)
                .confirmed(height > 0L)
                .inMempool(height == 0L)
                .allInputsConfirmed(height != -1L)
                .build();
    }

    @NonNull
    String txHash;

    Long height;

    boolean confirmed;
    boolean inMempool;
    boolean allInputsConfirmed;

    @Override
    public Optional<Long> getHeight() {
        return Optional.ofNullable(height);
    }
}
