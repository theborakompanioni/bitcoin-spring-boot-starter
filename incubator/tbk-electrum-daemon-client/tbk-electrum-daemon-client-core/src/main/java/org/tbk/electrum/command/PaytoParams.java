package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class PaytoParams {

    @NonNull
    @JsonProperty("destination")
    String destination;

    @NonNull
    @JsonProperty("amount")
    String amount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("fee")
    String fee;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("feerate")
    String feeRate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("from_addr")
    String fromAddress;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("from_coins")
    Object fromCoins;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("change_addr")
    String changeAddress;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("nocheck")
    Boolean noCheck;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("unsigned")
    Boolean unsigned;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("addtransaction")
    Boolean addTransaction;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("rbf")
    Boolean replaceByFee;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("locktime")
    Long locktime;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("wallet_path")
    String walletPath;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("password")
    String password;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("forgetconfig")
    Boolean forgetconfig;
}