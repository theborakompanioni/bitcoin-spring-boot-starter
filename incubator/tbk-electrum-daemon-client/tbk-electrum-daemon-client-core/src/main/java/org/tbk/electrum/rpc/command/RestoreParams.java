package org.tbk.electrum.rpc.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class RestoreParams {

    @NonNull
    @JsonProperty("key")
    String text;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("passphrase")
    String passphrase;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("encrypt_file")
    Boolean encryptFile;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("password")
    String password;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("wallet_path")
    String walletPath;
}
