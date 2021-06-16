package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Optional;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class RawTransactionResponse {

    @JsonProperty("hex")
    String hex;

    @JsonProperty("complete")
    boolean complete;

    @JsonProperty("final")
    boolean finalized;

    @JsonProperty("error")
    String error;

    public Optional<String> getError() {
        return Optional.ofNullable(error);
    }
}
