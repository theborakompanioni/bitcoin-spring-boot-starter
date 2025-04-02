package org.tbk.electrum.command;

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
 *     "auto_connect": true,
 *     "blockchain_height": -1,
 *     "connected": false,
 *     "default_wallet": "/home/electrum/.electrum/regtest/wallets/default_wallet",
 *     "fee_per_kb": 150000,
 *     "network": "regtest",
 *     "path": "/home/electrum/.electrum/regtest",
 *     "server": "host.testcontainers.internal",
 *     "server_height": 0,
 *     "spv_nodes": 0,
 *     "version": "4.5.8"
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

    @JsonProperty("fee_per_kb")
    int feePerKb;

    @JsonProperty("default_wallet")
    String defaultWallet;
}
