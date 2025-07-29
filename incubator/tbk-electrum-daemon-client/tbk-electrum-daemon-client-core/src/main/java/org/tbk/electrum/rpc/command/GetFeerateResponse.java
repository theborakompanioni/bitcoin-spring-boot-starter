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
 * or
 * {
 *     "description": "10. sat/vbyte",
 *     "policy": "feerate:10000",
 *     "sat/kvB": 10000,
 *     "tooltip": "0.00 vMB from tip\nLow fee"
 * }
 * or
 * {
 *     "method": "mempool",
 *     "sat/kvB": 1000,
 *     "tooltip": "2.00 vMB from tip",
 *     "value": 2000000
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
