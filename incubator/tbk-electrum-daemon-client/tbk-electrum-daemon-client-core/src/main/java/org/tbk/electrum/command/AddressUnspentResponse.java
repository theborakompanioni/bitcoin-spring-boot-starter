package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AddressUnspentResponse {

    @Data
    @Setter(AccessLevel.NONE)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Utxo {

        @JsonProperty("height")
        long height;

        @JsonProperty("tx_hash")
        String txHash;

        @JsonProperty("tx_pos")
        int txPos;

        /**
         * Value in Satoshi
         * <p>
         * At the time of writing 1 BTC is 10^8 Satoshi ;-)
         * 1 BTC := 100_000_000 Satoshi
         */
        @JsonProperty("value")
        long value;
    }
}
