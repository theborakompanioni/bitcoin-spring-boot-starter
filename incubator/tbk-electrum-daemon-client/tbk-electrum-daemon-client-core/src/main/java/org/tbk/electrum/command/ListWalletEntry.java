package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * <pre>
 * ~ $ electrum --regtest list_wallets
 * [
 *     {
 *         "path": "/home/electrum/.electrum/regtest/wallets/default_wallet",
 *         "synchronized": true,
 *         "unlocked": false
 *     }
 * ]
 * </pre>
 */
@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListWalletEntry {

    @JsonProperty("path")
    String path;

    @JsonProperty("synchronized")
    Boolean synced;

    @JsonProperty("unlocked")
    Boolean unlocked;
}
