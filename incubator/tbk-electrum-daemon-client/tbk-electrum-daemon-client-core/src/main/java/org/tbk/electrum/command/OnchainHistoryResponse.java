package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a history call which includes onchain transactions.
 *
 * <p>Example response with data from electrum:
 * <p>~ $ electrum --regtest onchain_history --show_addresses
 * [
 *     {
 *         "amount_sat": 5000000000,
 *         "bc_balance": "50.",
 *         "bc_value": "50.",
 *         "confirmations": 127,
 *         "date": "2025-07-06 13:29",
 *         "fee_sat": null,
 *         "group_id": null,
 *         "height": 1,
 *         "incoming": true,
 *         "inputs": [
 *             {
 *                 "coinbase": false,
 *                 "nsequence": 4294967295,
 *                 "prevout_hash": "0000000000000000000000000000000000000000000000000000000000000000",
 *                 "prevout_n": 4294967295,
 *                 "scriptSig": "5100",
 *                 "witness": [
 *                     "0000000000000000000000000000000000000000000000000000000000000000"
 *                 ]
 *             }
 *         ],
 *         "label": "",
 *         "monotonic_timestamp": 1751837382,
 *         "outputs": [
 *             {
 *                 "address": "bcrt1q0xtrupsjmqr7u7xz4meufd3a8pt6v553m8nmvz",
 *                 "value_sat": 5000000000
 *             },
 *             {
 *                 "address": "SCRIPT 6a24aa21a9ede2f61c3f71d1defd3fa999dfa36953755c690689799962b48bebd836974e8cf9",
 *                 "value_sat": 0
 *             }
 *         ],
 *         "timestamp": 1751837382,
 *         "txid": "5d62a473c9643969d9da210e2779eec31128110ffd9199f277d8c9af5d29fa94",
 *         "txpos_in_block": 0,
 *         "wanted_height": null
 *     },
 *     {
 *         "amount_sat": 5000000000,
 *         "bc_balance": "100.",
 *         "bc_value": "50.",
 *         "confirmations": 126,
 *         "date": "2025-07-06 13:29",
 *         "fee_sat": null,
 *         "group_id": null,
 *         "height": 2,
 *         "incoming": true,
 *         "inputs": [
 *             {
 *                 "coinbase": false,
 *                 "nsequence": 4294967295,
 *                 "prevout_hash": "0000000000000000000000000000000000000000000000000000000000000000",
 *                 "prevout_n": 4294967295,
 *                 "scriptSig": "5200",
 *                 "witness": [
 *                     "0000000000000000000000000000000000000000000000000000000000000000"
 *                 ]
 *             }
 *         ],
 *         "label": "",
 *         "monotonic_timestamp": 1751837383,
 *         "outputs": [
 *             {
 *                 "address": "bcrt1q0xtrupsjmqr7u7xz4meufd3a8pt6v553m8nmvz",
 *                 "value_sat": 5000000000
 *             },
 *             {
 *                 "address": "SCRIPT 6a24aa21a9ede2f61c3f71d1defd3fa999dfa36953755c690689799962b48bebd836974e8cf9",
 *                 "value_sat": 0
 *             }
 *         ],
 *         "timestamp": 1751837383,
 *         "txid": "3b7b4994f7977ef5f6cd8de11b9d8c07601da344d68e16b3e1dc2fdf25dfdc70",
 *         "txpos_in_block": 0,
 *         "wanted_height": null
 *     },
 *     [...]
 * ]
 */
@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnchainHistoryResponse {

    @JsonProperty("transactions")
    List<HistoricTransaction> transactions;

    /**
     * {
     *         "amount_sat": 5000000000,
     *         "bc_balance": "50.",
     *         "bc_value": "50.",
     *         "confirmations": 127,
     *         "date": "2025-07-06 13:29",
     *         "fee_sat": null,
     *         "group_id": null,
     *         "height": 1,
     *         "incoming": true,
     *         "inputs": [
     *             {
     *                 "coinbase": false,
     *                 "nsequence": 4294967295,
     *                 "prevout_hash": "0000000000000000000000000000000000000000000000000000000000000000",
     *                 "prevout_n": 4294967295,
     *                 "scriptSig": "5100",
     *                 "witness": [
     *                     "0000000000000000000000000000000000000000000000000000000000000000"
     *                 ]
     *             }
     *         ],
     *         "label": "",
     *         "monotonic_timestamp": 1751837382,
     *         "outputs": [
     *             {
     *                 "address": "bcrt1q0xtrupsjmqr7u7xz4meufd3a8pt6v553m8nmvz",
     *                 "value_sat": 5000000000
     *             },
     *             {
     *                 "address": "SCRIPT 6a24aa21a9ede2f61c3f71d1defd3fa999dfa36953755c690689799962b48bebd836974e8cf9",
     *                 "value_sat": 0
     *             }
     *         ],
     *         "timestamp": 1751837382,
     *         "txid": "5d62a473c9643969d9da210e2779eec31128110ffd9199f277d8c9af5d29fa94",
     *         "txpos_in_block": 0,
     *         "wanted_height": null
     *     }
     */
    @Value
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HistoricTransaction {
        @JsonProperty("amount_sat")
        Long amountSat;
        
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

        /**
         * {
         *      "coinbase": false,
         *      "nsequence": 4294967295,
         *      "prevout_hash": "0000000000000000000000000000000000000000000000000000000000000000",
         *      "prevout_n": 4294967295,
         *      "scriptSig": "5100",
         *      "witness": [
         *          "0000000000000000000000000000000000000000000000000000000000000000"
         *      ]
         *  }
         */
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
            List<String> witness;
        }

        /**
         *  {
         *      "address": "bcrt1q0xtrupsjmqr7u7xz4meufd3a8pt6v553m8nmvz",
         *      "value_sat": 5000000000
         *  }
         */
        @Value
        @Builder
        @Jacksonized
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Output {
            @JsonProperty("address")
            String address;

            @JsonProperty("value_sat")
            Long valueSat;
        }
    }
}
