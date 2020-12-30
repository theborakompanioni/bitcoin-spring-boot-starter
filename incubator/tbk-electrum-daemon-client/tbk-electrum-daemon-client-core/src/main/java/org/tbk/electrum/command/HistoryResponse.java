package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a history call which includes a short summary
 * and transactions.
 * <p>
 * Can also have empty fields:
 * e.g.
 * {
 * "summary": {},
 * "transactions": []
 * }
 * <p>
 * Example response with data from electrum:
 * <p>
 * {
 * "summary": {
 * "end_balance": "0.",
 * "end_date": null,
 * "from_height": null,
 * "incoming": "0.01532938",
 * "outgoing": "0.01532938",
 * "start_balance": "0.",
 * "start_date": null,
 * "to_height": null
 * },
 * "transactions": [
 * {
 * "balance": "0.0013",
 * "confirmations": 296588,
 * "date": "2014-11-09 10:23",
 * "height": 329232,
 * "incoming": true,
 * "inputs": [
 * {
 * "prevout_hash": "f23a23c48cffe1776d6bc1bca80d8e2e87b8085dfffc055c660fa21ec1d50c6d",
 * "prevout_n": 1
 * }
 * ],
 * "label": "",
 * "outputs": [
 * {
 * "address": "1K47dfy7224ay3icqt4feqwPDujn7kf9W2",
 * "value": "0.38225868"
 * },
 * {
 * "address": "1LqBGSKuX5yYUonjxT5qGfpUsXKYYWeabA",
 * "value": "0.0013"
 * }
 * ],
 * "timestamp": 1415528603,
 * "txid": "ddf793869ba325d06882b78a8c599ef8d512d01d716a8fdd30e51a9e268d6820",
 * "txpos_in_block": 588,
 * "value": "0.0013"
 * },
 * {
 * "balance": "0.0026",
 * "confirmations": 296579,
 * "date": "2014-11-09 11:31",
 * "height": 329241,
 * "incoming": true,
 * "inputs": [
 * {
 * "prevout_hash": "cfe97a5ffc2ef4e7fe7973f3446a9dd460c763fd6d9adcebaee9af6940f727ce",
 * "prevout_n": 1
 * }
 * ],
 * "label": "",
 * "outputs": [
 * {
 * "address": "1Ak8PffB2meyfYnbXZR9EGfLfFZVpzJvQP",
 * "value": "0.0013"
 * },
 * {
 * "address": "1DTxdeWvLfzgAkCNRNVkPRrfECxUvqsK5F",
 * "value": "0.0058"
 * }
 * ],
 * "timestamp": 1415532699,
 * "txid": "d6f136091b72cb4fcacce00c76cf6244c184f5654e4c554926bc2de072fc5def",
 * "txpos_in_block": 152,
 * "value": "0.0013"
 * },
 * {
 * "balance": "0.0039",
 * "confirmations": 296425,
 * "date": "2014-11-10 10:24",
 * "height": 329395,
 * "incoming": true,
 * "inputs": [
 * {
 * "prevout_hash": "853542e895e160bd57d8bc483314d60000555d53a1937f8df158588d90cab58c",
 * "prevout_n": 1
 * }
 * ],
 * "label": "",
 * "outputs": [
 * {
 * "address": "1LqBGSKuX5yYUonjxT5qGfpUsXKYYWeabA",
 * "value": "0.0013"
 * },
 * {
 * "address": "1DTxdeWvLfzgAkCNRNVkPRrfECxUvqsK5F",
 * "value": "0.00306095"
 * }
 * ],
 * "timestamp": 1415615095,
 * "txid": "2d37562f17d3976aee8a64e67125b0d6350bb10394c4d443d34b4c30b4c10b28",
 * "txpos_in_block": 121,
 * "value": "0.0013"
 * },
 * {
 * "balance": "0.0052",
 * "confirmations": 296408,
 * "date": "2014-11-10 13:10",
 * "height": 329412,
 * "incoming": true,
 * "inputs": [
 * {
 * "prevout_hash": "c72b480e56db6390668f4848b3314abec36aa2c479715810da80405119e94faf",
 * "prevout_n": 0
 * }
 * ],
 * "label": "",
 * "outputs": [
 * {
 * "address": "1LqBGSKuX5yYUonjxT5qGfpUsXKYYWeabA",
 * "value": "0.0013"
 * },
 * {
 * "address": "1DTxdeWvLfzgAkCNRNVkPRrfECxUvqsK5F",
 * "value": "0.00278"
 * }
 * ],
 * "timestamp": 1415625013,
 * "txid": "78aacf484f74e9880fb6158d8f4da2759ce1ee17513196dde0d584cc7fbbebb3",
 * "txpos_in_block": 88,
 * "value": "0.0013"
 * },
 * {
 * "balance": "0.00419",
 * "confirmations": 295252,
 * "date": "2014-11-18 12:03",
 * "height": 330568,
 * "incoming": false,
 * "inputs": [
 * {
 * "prevout_hash": "ddf793869ba325d06882b78a8c599ef8d512d01d716a8fdd30e51a9e268d6820",
 * "prevout_n": 1
 * }
 * ],
 * "label": "",
 * "outputs": [
 * {
 * "address": "1AhN6rPdrMuKBGFDKR1k9A8SCLYaNgXhty",
 * "value": "0.001"
 * },
 * {
 * "address": "1J3J6EvPrv8q6AC3VCjWV45Uf3nssNMRtH",
 * "value": "0.00029"
 * }
 * ],
 * "timestamp": 1416312229,
 * "txid": "7ef8f3d70d4c152285ba68e804e7c91545dacc42c2f11d14ab697ca9bec757b5",
 * "txpos_in_block": 505,
 * "value": "-0.00101"
 * },
 * ]
 * }
 */
@Data
@Setter(AccessLevel.NONE)
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryResponse {

    @JsonProperty("summary")
    private Summary summary;

    @JsonProperty("transactions")
    private List<Transaction> transactions;

    @Data
    @Setter(AccessLevel.NONE)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Summary {

        @Nullable
        @JsonProperty("start_balance")
        private String startBalance;

        @Nullable
        @JsonProperty("end_balance")
        private String endBalance;

        @Nullable
        @JsonProperty("incoming")
        private String incoming;

        @Nullable
        @JsonProperty("outgoing")
        private String outgoing;

        @Nullable
        @JsonProperty("start_date")
        // TODO: probably "String"? only ever got "null" from electrum
        private Object startDate;

        @Nullable
        @JsonProperty("end_date")
        // TODO: probably "String"? only ever got "null" from electrum
        private Object endDate;

        @Nullable
        @JsonProperty("from_height")
        // TODO: probably "long"? only ever got "null" from electrum
        private Object fromHeight;

        @Nullable
        @JsonProperty("to_height")
        // TODO: probably "long"? only ever got "null" from electrum
        private Object toHeight;
    }


    @Data
    @Setter(AccessLevel.NONE)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Transaction {

        @JsonProperty("balance")
        private String balance;

        @JsonProperty("confirmations")
        private long confirmations;

        // timestamp e.g. 1415528603
        @Nullable
        @JsonProperty("timestamp")
        private Long timestamp;

        // pattern e.g. 2014-11-09 10:23
        @Nullable
        @JsonProperty("date")
        private String date;

        @Nullable
        @JsonProperty("height")
        private Long height;

        @JsonProperty("incoming")
        private boolean incoming;

        @JsonProperty("label")
        private String label;

        @JsonProperty("txid")
        private String txId;

        @JsonProperty("txpos_in_block")
        private Integer txPosInBlock;

        @JsonProperty("value")
        private String value;

        @Nullable
        @JsonProperty("inputs")
        private List<Input> inputs;

        @Nullable
        @JsonProperty("outputs")
        private List<Output> outputs;

        @Data
        @Setter(AccessLevel.NONE)
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Input {
            @JsonProperty("prevout_hash")
            private String prevoutHash;

            @JsonProperty("prevout_n")
            private long prevoutN;
        }

        @Data
        @Setter(AccessLevel.NONE)
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Output {
            @JsonProperty("address")
            private String address;

            @JsonProperty("value")
            private String value;
        }
    }
}
