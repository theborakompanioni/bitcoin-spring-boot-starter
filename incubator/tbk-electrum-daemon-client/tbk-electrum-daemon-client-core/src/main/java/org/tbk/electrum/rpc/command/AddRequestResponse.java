package org.tbk.electrum.rpc.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.annotation.Nullable;
import java.util.List;

/**
 * {
 * "URI": null,
 * "address": "<address>",
 * "amount_BTC": "unknown", // or btc string like "0.000011"
 * "amount_sat": null, // or 2000
 * "expiry": 3600,
 * "is_lightning": false,
 * "message": "",
 * "request_id": "4582dd8ca5",
 * "status": 0,
 * "status_str": "Expires in about 1 hour",
 * "timestamp": 1753797643,
 * "tx_hashes": []
 * }
 */
@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddRequestResponse {
    @Nullable
    @JsonProperty("URI")
    String uri;

    @JsonProperty("address")
    String address;

    @Nullable
    @JsonProperty("amount_sat")
    Long amount;

    @JsonProperty("expiry")
    Long expiry;

    @JsonProperty("is_lightning")
    boolean lightning;

    @JsonProperty("message")
    String message;

    @JsonProperty("request_id")
    String requestId;

    @JsonProperty("status")
    int status;

    @JsonProperty("status_str")
    String statusMessage;

    @JsonProperty("timestamp")
    long timestamp;

    @JsonProperty("tx_hashes")
    List<String> txHashes;
}
