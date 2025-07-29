package org.tbk.electrum.rpc.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.tbk.electrum.model.TxoValue;

import javax.annotation.Nullable;
import java.time.Duration;

@Value
@Builder
public class AddRequestParams {

    @NonNull
    @JsonProperty("amount")
    TxoValue amount;

    @Nullable
    @JsonProperty("memo")
    String memo;

    @Nullable
    @JsonProperty("expiry")
    Duration expiry;

    @Builder.Default
    @Nullable
    @JsonProperty("force")
    Boolean force = Boolean.TRUE;

    @Nullable
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("wallet_path")
    String walletPath;
}
