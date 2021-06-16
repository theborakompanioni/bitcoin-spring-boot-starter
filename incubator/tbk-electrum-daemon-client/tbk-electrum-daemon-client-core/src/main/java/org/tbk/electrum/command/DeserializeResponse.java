package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeserializeResponse {

    @JsonProperty("partial")
    boolean partial;

    @JsonProperty("version")
    int version;

    @JsonProperty("segwit_ser")
    boolean segwit;

    @JsonProperty("lockTime")
    int lockTime;

    @JsonProperty("inputs")
    List<Input> inputs;

    @JsonProperty("outputs")
    List<Output> outputs;

    @Value
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Input {
        @JsonProperty("prevout_hash")
        String prevoutHash;

        @JsonProperty("prevout_n")
        int prevoutN;

        @JsonProperty("scriptSig")
        String scriptSig;

        @JsonProperty("sequence")
        long sequence;

        @JsonProperty("type")
        String type;

        @JsonProperty("address")
        String address;

        @JsonProperty("num_sig")
        int numSig;

        @JsonProperty("x_pubkeys")
        List<String> xpubKeys;

        @JsonProperty("pubkeys")
        List<String> pubkeys;

        // TODO: we are getting an exception on deserialization:
        //  "Cannot deserialize instance of `java.util.ArrayList<java.lang.Object>` out of START_OBJECT token"
        //  Commented our for now -> take a look at electrums source to understand what is returned. 2020-04-23
        // @JsonProperty("signatures")
        // private List<String> signatures;

        @JsonProperty("witness")
        String witness;

        @JsonProperty("witness_version")
        Integer witnessVersion;

        @JsonProperty("witness_script")
        String witnessScript;

        @JsonProperty("redeem_script")
        String redeemScript;

        @JsonProperty("value")
        Long value;
    }

    @Value
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Output {
        @JsonProperty("value")
        long value;

        @JsonProperty("type")
        int type;

        @JsonProperty("address")
        String address;

        @JsonProperty("scriptPubKey")
        String scriptPubKey;
        
        @JsonProperty("prevout_n")
        int prevoutN;
    }
}
