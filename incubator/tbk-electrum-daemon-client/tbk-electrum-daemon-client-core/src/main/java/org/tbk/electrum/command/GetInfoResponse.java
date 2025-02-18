package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

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
