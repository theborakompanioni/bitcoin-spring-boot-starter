package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GetBalanceParams {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("wallet")
    String walletPath;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("forgetconfig")
    Boolean forgetconfig;
}
