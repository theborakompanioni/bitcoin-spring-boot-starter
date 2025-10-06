package org.tbk.electrum.rpc.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnchainHistoryParams {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("year")
    Integer year;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("from_height")
    Long fromHeight;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("to_height")
    Long toHeight;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("wallet_path")
    String walletPath;
}
