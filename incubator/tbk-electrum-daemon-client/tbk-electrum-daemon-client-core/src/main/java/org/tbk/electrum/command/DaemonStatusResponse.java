package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;
import java.util.Optional;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class DaemonStatusResponse {

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

    /**
     * Path to currently loaded wallet.
     */
    @JsonProperty("current_wallet")
    String currentWallet;

    public Optional<String> getCurrentWallet() {
        return Optional.ofNullable(currentWallet);
    }
}
