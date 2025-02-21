package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.tbk.electrum.model.RawTx;

@Value
@Builder
public class SignTransactionParams {

    public static SignTransactionParams.SignTransactionParamsBuilder of(RawTx tx) {
        return SignTransactionParams.builder().tx(tx.getHex());
    }

    @NonNull
    @JsonProperty("tx")
    String tx;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("password")
    String password;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("wallet_path")
    String walletPath;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("forgetconfig")
    Boolean forgetconfig;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("iknowwhatimdoing")
    Boolean iknowwhatimdoing;
}
