package org.tbk.electrum.rpc.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * {
 * "address": "bcrt1qyxgzvy4k99fnpzhxmljtp65n8852dls48lujka",
 * "bip32_paths": {},
 * "coinbase": true,
 * "desc": null,
 * "height": 6,
 * "nsequence": 4294967294,
 * "prevout_hash": "54de7bdf8bb370244a9a23f56f7d5e32ed3c1fc94f28519f9245b7e80c1f98a1",
 * "prevout_n": 0,
 * "redeem_script": null,
 * "sighash": null,
 * "sigs_ecdsa": {},
 * "slip_19_ownership_proof": null,
 * "tap_key_sig": null,
 * "tap_merkle_root": null,
 * "unknown_psbt_fields": {},
 * "utxo": null,
 * "value": "50",
 * "witness_script": null,
 * "witness_utxo": null
 * }
 */
public class ListUnspentResponse {
    @Value
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ListUnspentEntry {
        @JsonProperty("address")
        String address;

        @JsonProperty("coinbase")
        boolean coinbase;

        @JsonProperty("height")
        long height;

        @JsonProperty("nsequence")
        long nsequence;

        @JsonProperty("prevout_hash")
        String prevoutHash;

        @JsonProperty("prevout_n")
        int prevoutN;

        @JsonProperty("value")
        String value;
    }

}
