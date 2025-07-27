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
 * "inputs": [
 * {
 * "coinbase": false,
 * "nsequence": 4294967293,
 * "prevout_hash": "37a90988fc3b99ea9626febbd5a9f451c26e229c29f1f501b12c116253b59756",
 * "prevout_n": 0,
 * "scriptSig": "",
 * "witness": [
 * "30440220097a5646c942e0aa9548ca55da657f9ab79572f2652b41a409e64955fb72a4c602205d2e740175a4e1c86f2d8dd60a15524794b1a7f66fb7f7e4f8c8417d8a868c1b01",
 * "0340bfedc58e483a73ec30cbdbe6b77cd37472c0b8d9228734e466dc428ee590f4"
 * ]
 * }
 * ],
 * "locktime": 105,
 * "outputs": [
 * {
 * "address": "bcrt1qyxgzvy4k99fnpzhxmljtp65n8852dls48lujka",
 * "scriptpubkey": "001421902612b62953308ae6dfe4b0ea9339e8a6fe15",
 * "value_sats": 21000
 * },
 * {
 * "address": "bcrt1q0jllwku5ljdwfa0fxpsxtl2fcqex0apptnsw9n",
 * "scriptpubkey": "00147cbff75b94fc9ae4f5e9306065fd49c03267f421",
 * "value_sats": 19999929000
 * }
 * ],
 * "version": 2
 * }
 */
@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeserializeResponse {

    // TODO: verify it is part of the response
    @Nullable
    @JsonProperty("partial")
    Boolean partial;

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
        @JsonProperty("coinbase")
        boolean coinbase;

        @JsonProperty("nsequence")
        long sequence;

        @JsonProperty("prevout_hash")
        String prevoutHash;

        // prevout is 4294967295 for coinbase inputs, so field must be of type `long`
        @JsonProperty("prevout_n")
        long prevoutN;

        @JsonProperty("scriptSig")
        String scriptSig;

        @JsonProperty("type")
        String type;

        // TODO: we are getting an exception on deserialization:
        //  "Cannot deserialize instance of `java.util.ArrayList<java.lang.Object>` out of START_OBJECT token"
        //  Commented our for now -> take a look at electrums source to understand what is returned. 2020-04-23
        // @JsonProperty("signatures")
        // private List<String> signatures;

        @JsonProperty("witness")
        List<String> witness;

        // TODO: verify it is part of the response
        @Nullable
        @JsonProperty("address")
        String address;

        // TODO: verify it is part of the response
        @Nullable
        @JsonProperty("num_sig")
        Integer numSig;

        // TODO: verify it is part of the response
        @Nullable
        @JsonProperty("x_pubkeys")
        List<String> xpubKeys;

        // TODO: verify it is part of the response
        @Nullable
        @JsonProperty("pubkeys")
        List<String> pubkeys;

        // TODO: verify it is part of the response
        @Nullable
        @JsonProperty("witness_version")
        Integer witnessVersion;

        // TODO: verify it is part of the response
        @Nullable
        @JsonProperty("witness_script")
        String witnessScript;

        // TODO: verify it is part of the response
        @Nullable
        @JsonProperty("redeem_script")
        String redeemScript;

        // TODO: verify it is part of the response
        @Nullable
        @JsonProperty("value")
        Long value;
    }

    @Value
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Output {
        @JsonProperty("value_sats")
        long value;

        @JsonProperty("address")
        String address;

        @JsonProperty("scriptpubkey")
        String scriptPubKey;

        // TODO: verify it is part of the response
        @Nullable
        @JsonProperty("type")
        Integer type;

        // TODO: verify it is part of the response
        @Nullable
        @JsonProperty("prevout_n")
        Integer prevoutN;
    }
}
