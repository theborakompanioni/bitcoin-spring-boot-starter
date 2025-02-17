package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a history call which includes a short summary and transactions.
 *
 * <p>Can also have empty fields:
 * e.g.
 * {
 * "summary": {},
 * "transactions": []
 * }
 *
 * <p>Example response with data from electrum:
 * <p>{
 *     "summary": {
 *         "begin": {
 *             "BTC_balance": "0.",
 *             "block_height": 0,
 *             "date": "2025-02-17 16:52"
 *         },
 *         "end": {
 *             "BTC_balance": "7700.",
 *             "block_height": 159,
 *             "date": "2025-02-17 16:57"
 *         },
 *         "flow": {
 *             "BTC_incoming": "7700.",
 *             "BTC_outgoing": "0."
 *         }
 *     },
 *     "transactions": [
 *         {
 *             "bc_balance": "50.",
 *             "bc_value": "50.",
 *             "confirmations": 159,
 *             "date": "2025-02-17 16:52",
 *             "fee": null,
 *             "fee_sat": null,
 *             "height": 1,
 *             "incoming": true,
 *             "inputs": [
 *                 {
 *                     "coinbase": false,
 *                     "nsequence": 4294967295,
 *                     "prevout_hash": "0000000000000000000000000000000000000000000000000000000000000000",
 *                     "prevout_n": 4294967295,
 *                     "scriptSig": "5100",
 *                     "witness": "01200000000000000000000000000000000000000000000000000000000000000000"
 *                 }
 *             ],
 *             "label": "",
 *             "monotonic_timestamp": 1739811157,
 *             "outputs": [
 *                 {
 *                     "address": "bcrt1q0xtrupsjmqr7u7xz4meufd3a8pt6v553m8nmvz",
 *                     "value": "50."
 *                 },
 *                 {
 *                     "address": "SCRIPT 6a24aa21a9ede2f61c3f71d1defd3fa999dfa36953755c690689799962b48bebd836974e8cf9",
 *                     "value": "0."
 *                 }
 *             ],
 *             "timestamp": 1739811157,
 *             "txid": "5d62a473c9643969d9da210e2779eec31128110ffd9199f277d8c9af5d29fa94",
 *             "txpos_in_block": 0
 *         },
 *         [...]
 *     ]
 * }
 */
@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryResponse {

    @JsonProperty("summary")
    Summary summary;

    @JsonProperty("transactions")
    List<Transaction> transactions;

    @Value
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Summary {

        @Value
        @Builder
        @Jacksonized
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class SummaryTime {
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
        public static class SummaryFlow {
            @JsonProperty("BTC_incoming")
            String incoming;

            @JsonProperty("BTC_outgoing")
            String outgoing;
        }

        @Nullable
        @JsonProperty("begin")
        SummaryTime begin;

        @Nullable
        @JsonProperty("end")
        SummaryTime end;

        @Nullable
        @JsonProperty("flow")
        SummaryFlow flow;
    }
    @Value
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Transaction {

        @JsonProperty("bc_balance")
        String balance;

        @JsonProperty("bc_value")
        String value;

        @JsonProperty("confirmations")
        long confirmations;

        // pattern e.g. 2014-11-09 10:23
        @Nullable
        @JsonProperty("date")
        String date;

        @Nullable
        @JsonProperty("height")
        Long height;

        @JsonProperty("incoming")
        boolean incoming;

        @Nullable
        @JsonProperty("inputs")
        List<Input> inputs;

        @JsonProperty("label")
        String label;

        // timestamp e.g. 1415528603
        @Nullable
        @JsonProperty("monotonic_timestamp")
        Long monotonicTimestamp;

        @Nullable
        @JsonProperty("outputs")
        List<Output> outputs;

        // timestamp e.g. 1415528603
        @Nullable
        @JsonProperty("timestamp")
        Long timestamp;

        @JsonProperty("txid")
        String txId;

        @JsonProperty("txpos_in_block")
        Integer txPosInBlock;

        @Value
        @Builder
        @Jacksonized
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Input {
            @JsonProperty("coinbase")
            boolean coinbase;

            @Nullable
            @JsonProperty("nsequence")
            Long nsequence;

            @JsonProperty("prevout_hash")
            String prevoutHash;

            @JsonProperty("prevout_n")
            long prevoutN;

            @Nullable
            @JsonProperty("scriptSig")
            String scriptSig;

            @Nullable
            @JsonProperty("witness")
            String witness;
        }

        @Value
        @Builder
        @Jacksonized
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Output {
            @JsonProperty("address")
            String address;

            @JsonProperty("value")
            String value;
        }
    }
}
