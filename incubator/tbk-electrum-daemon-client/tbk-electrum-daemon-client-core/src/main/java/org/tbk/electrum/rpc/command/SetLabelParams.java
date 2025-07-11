package org.tbk.electrum.rpc.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SetLabelParams {

    @NonNull
    @JsonProperty("key")
    String key;

    @NonNull
    @JsonProperty("label")
    String label;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("wallet_path")
    String walletPath;
}
