package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.annotation.Nullable;
import java.util.Optional;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceResponse {

    @JsonProperty("confirmed")
    String confirmed;

    @Nullable
    @JsonProperty("unconfirmed")
    String unconfirmed;

    @Nullable
    @JsonProperty("unmatured")
    String unmatured;

    @Nullable
    @JsonProperty("lightning")
    String lightning;

    public Optional<String> getUnconfirmed() {
        return Optional.ofNullable(unconfirmed);
    }

    public Optional<String> getUnmatured() {
        return Optional.ofNullable(unmatured);
    }
    public Optional<String> getLightning() {
        return Optional.ofNullable(lightning);
    }
}
