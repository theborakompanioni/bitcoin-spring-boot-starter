package org.tbk.electrum.rpc.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MakeSeedParams {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("seed_type")
    String seedType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("language")
    String language;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("nbits")
    Integer nbits;
}
