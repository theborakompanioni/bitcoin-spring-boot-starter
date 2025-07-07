package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * {
 * "begin": {
 * "BTC_balance": "0.",
 * "block_height": 0,
 * "date": "2025-07-06 13:14"
 * },
 * "end": {
 * "BTC_balance": "6550.",
 * "block_height": 131,
 * "date": "2025-07-06 13:17"
 * },
 * "flow": {
 * "BTC_incoming": "6550.",
 * "BTC_outgoing": "0."
 * }
 * }
 */
@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnchainCapitalGainsResponse {

    @Nullable
    @JsonProperty("begin")
    PointInTimeStats begin;

    @Nullable
    @JsonProperty("end")
    PointInTimeStats end;

    @Nullable
    @JsonProperty("flow")
    FlowStats flow;

    public Optional<PointInTimeStats> getBegin() {
        return Optional.ofNullable(begin);
    }

    public Optional<PointInTimeStats> getEnd() {
        return Optional.ofNullable(end);
    }

    public Optional<FlowStats> getFlow() {
        return Optional.ofNullable(flow);
    }

    @Value
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PointInTimeStats {
        @JsonProperty("BTC_balance")
        String balance;

        @Nullable
        @JsonProperty("block_height")
        Long blockHeight;

        @Nullable
        @JsonProperty("date")
        String date;
    }

    @Value
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlowStats {
        @JsonProperty("BTC_incoming")
        String incoming;

        @JsonProperty("BTC_outgoing")
        String outgoing;
    }
}
