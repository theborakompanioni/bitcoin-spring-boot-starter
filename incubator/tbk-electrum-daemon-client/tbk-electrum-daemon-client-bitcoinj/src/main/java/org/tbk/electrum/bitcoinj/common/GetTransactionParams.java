package org.tbk.electrum.bitcoinj.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Sha256Hash;

@Value
@Builder
public class GetTransactionParams {

    @NonNull
    @JsonProperty("txid")
    Sha256Hash txid;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("wallet_path")
    String walletPath;
}
