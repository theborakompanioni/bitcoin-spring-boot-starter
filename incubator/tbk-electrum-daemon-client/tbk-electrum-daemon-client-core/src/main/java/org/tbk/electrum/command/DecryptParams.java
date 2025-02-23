package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class DecryptParams {

    /**
     * Public key (in hex)
     */
    @NonNull
    @JsonProperty("pubkey")
    String publicKey;

    @NonNull
    @JsonProperty("encrypted")
    String encryptedMessage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("wallet_path")
    String walletPath;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("password")
    String password;
}