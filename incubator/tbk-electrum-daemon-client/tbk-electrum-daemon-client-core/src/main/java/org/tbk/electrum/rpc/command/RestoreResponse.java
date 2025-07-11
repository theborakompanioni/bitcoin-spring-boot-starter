package org.tbk.electrum.rpc.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * {
 *     "msg": "This wallet was restored offline. It may contain more addresses than displayed. Start a daemon and use load_wallet to sync its history.",
 *     "path": "/home/electrum/.electrum/regtest/wallets/test_wallet"
 * }
 */
@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestoreResponse {
    @JsonProperty("msg")
    String message;

    @JsonProperty("path")
    String path;
}
