package org.tbk.electrum.rpc.command;

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
        // > 0 if all inputs are confirmed, and -1 otherwise.
        // see:
        // - https://electrumx.readthedocs.io/en/latest/protocol-methods.html#blockchain-scripthash-get-history
        // - https://electrumx.readthedocs.io/en/latest/protocol-methods.html#blockchain-scripthash-get-mempool
        @JsonProperty("height")
        long height;

        @JsonProperty("tx_hash")
        String txHash;

        public boolean isConfirmed() {
            return height >= 0L;
        }
    }
}
