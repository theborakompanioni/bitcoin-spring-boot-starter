package org.tbk.electrum.rpc.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * getinfo: <a href="https://github.com/spesmilo/electrum/blob/4.5.8/electrum/commands.py#L217">electrum/commands.py#L217</a>
 *
 * <pre>
 * ~ $ electrum --regtest getinfo
 * {
 *     "auto_connect": false,
 *     "blockchain_height": -1,
 *     "connected": true,
 *     "fee_estimates": {},
 *     "network": "regtest",
 *     "path": "/home/electrum/.electrum/regtest",
 *     "server": "electrumx_regtest",
 *     "server_height": 0,
 *     "spv_nodes": 1,
 *     "version": "4.6.0b1"
 * }
 * </pre>
 */
@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetInfoResponse {

    @JsonProperty("network")
    String network;

    @JsonProperty("path")
    String path;

    @JsonProperty("server")
    String server;

    @JsonProperty("blockchain_height")
    int blockchainHeight;

    @JsonProperty("server_height")
    int serverHeight;

    @JsonProperty("spv_nodes")
    int spvNodes;

    @JsonProperty("connected")
    boolean connected;

    @JsonProperty("auto_connect")
    boolean autoConnect;

    @JsonProperty("version")
    String version;

    // TODO: add type for fee estimates
    @JsonProperty("fee_estimates")
    Object feeEstimates;
}
