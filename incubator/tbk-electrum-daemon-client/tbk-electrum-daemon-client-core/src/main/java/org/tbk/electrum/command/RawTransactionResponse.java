package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Optional;

@Data
@Setter(AccessLevel.NONE)
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RawTransactionResponse {

    @JsonProperty("hex")
    private String hex;

    @JsonProperty("complete")
    private boolean complete;

    @JsonProperty("final")
    private boolean finalized;

    @JsonProperty("error")
    private String error;

    public Optional<String> getError() {
        return Optional.ofNullable(error);
    }
}
