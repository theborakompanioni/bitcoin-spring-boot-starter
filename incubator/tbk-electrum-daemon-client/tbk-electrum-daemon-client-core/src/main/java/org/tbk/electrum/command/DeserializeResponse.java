package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeserializeResponse {
    @JsonProperty("partial")
    private boolean partial;

    @JsonProperty("version")
    private int version;

    @JsonProperty("segwit_ser")
    private boolean segwit;

    @JsonProperty("lockTime")
    private int lockTime;

    @JsonProperty("inputs")
    private List<Input> inputs;

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
        private int prevoutN;

        @JsonProperty("scriptSig")
        private String scriptSig;

        @JsonProperty("sequence")
        private long sequence;

        @JsonProperty("type")
        private String type;

        @JsonProperty("address")
        private String address;

        @JsonProperty("num_sig")
        private int numSig;

        @JsonProperty("x_pubkeys")
        private List<String> xpubKeys;

        @JsonProperty("pubkeys")
        private List<String> pubkeys;

        // TODO: we are getting an exception on deserialization:
        //  "Cannot deserialize instance of `java.util.ArrayList<java.lang.Object>` out of START_OBJECT token"
        //  Commented our for now -> take a look at electrums source to understand what is returned. 2020-04-23
        // @JsonProperty("signatures")
        // private List<String> signatures;

        @JsonProperty("witness")
        private String witness;

        @JsonProperty("witness_version")
        private Integer witnessVersion;

        @JsonProperty("witness_script")
        private String witnessScript;

        @JsonProperty("redeem_script")
        private String redeemScript;

        @JsonProperty("value")
        private Long value;
    }

    @Data
    @Setter(AccessLevel.NONE)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Output {
        @JsonProperty("value")
        private long value;

        @JsonProperty("type")
        private int type;

        @JsonProperty("address")
        private String address;

        @JsonProperty("scriptPubKey")
        private String scriptPubKey;

        // TODO: whats the prevoutN in an output?
        @JsonProperty("prevout_n")
        private int prevoutN;
    }
}
