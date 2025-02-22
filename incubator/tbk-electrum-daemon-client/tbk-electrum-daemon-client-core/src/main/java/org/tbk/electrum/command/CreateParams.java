package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateParams {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("passphrase")
    String passphrase;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("encrypt_file")
    String encryptFile;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("seed_type")
    String seedType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("password")
    String password;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("wallet_path")
    String walletPath;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("forgetconfig")
    Boolean forgetconfig;
}
