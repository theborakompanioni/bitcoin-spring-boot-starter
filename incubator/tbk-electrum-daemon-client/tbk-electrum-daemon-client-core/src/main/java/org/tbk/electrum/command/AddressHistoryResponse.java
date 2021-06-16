package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AddressHistoryResponse {

    @Value
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {

        @JsonProperty("height")
        long height;

        @JsonProperty("tx_hash")
        String txHash;
    }
}
