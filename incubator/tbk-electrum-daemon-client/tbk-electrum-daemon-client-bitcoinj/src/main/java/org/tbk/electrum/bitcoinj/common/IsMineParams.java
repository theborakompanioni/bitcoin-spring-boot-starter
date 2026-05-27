package org.tbk.electrum.bitcoinj.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.bitcoinj.base.Address;

@Value
@Builder
public class IsMineParams {
    @NonNull
    @JsonProperty("address")
    Address address;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("wallet_path")
    String walletPath;
}
