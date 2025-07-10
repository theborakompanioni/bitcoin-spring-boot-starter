package org.tbk.electrum.rpc.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * <pre>
 * ~ $ electrum --regtest getfeerate
 * {
 *     "description": "Within 2 blocks",
 *     "policy": "eta:2",
 *     "sat/kvB": 150000,
 *     "tooltip": "150. sat/vbyte"
 * }
 * </pre>
 */
@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetFeerateResponse {

    @JsonProperty("description")
    String description;

    @JsonProperty("policy")
    String policy;

    @JsonProperty("sat/kvB")
    long satPerKvb;

    @JsonProperty("tooltip")
    String tooltip;
}
