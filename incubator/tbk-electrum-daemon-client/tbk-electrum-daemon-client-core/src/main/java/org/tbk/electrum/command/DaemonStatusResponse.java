package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;
import java.util.Optional;

@Data
@Setter(AccessLevel.NONE)
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DaemonStatusResponse {
    @JsonProperty("path")
    private String path;

    @JsonProperty("server")
    private String server;

    @JsonProperty("blockchain_height")
    private int blockchainHeight;

    @JsonProperty("server_height")
    private int serverHeight;

    @JsonProperty("spv_nodes")
    private int spvNodes;

    @JsonProperty("connected")
    private boolean connected;

    @JsonProperty("auto_connect")
    private boolean autoConnect;

    @JsonProperty("version")
    private String version;

    @JsonProperty("wallets")
    private Map<String, Boolean> wallets;

    @JsonProperty("fee_per_kb")
    private int feePerKb;

    /**
     * Path to currently loaded wallet
     */
    @JsonProperty("current_wallet")
    private String currentWallet;

    public Optional<String> getCurrentWallet() {
        return Optional.ofNullable(currentWallet);
    }
}
