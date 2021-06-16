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
public class BalanceResponse {

    @JsonProperty("confirmed")
    String confirmed;

    @JsonProperty("unconfirmed")
    String unconfirmed;

    @JsonProperty("unmatured")
    String unmatured;

    public Optional<String> getUnconfirmed() {
        return Optional.ofNullable(unconfirmed);
    }

    public Optional<String> getUnmatured() {
        return Optional.ofNullable(unmatured);
    }
}
