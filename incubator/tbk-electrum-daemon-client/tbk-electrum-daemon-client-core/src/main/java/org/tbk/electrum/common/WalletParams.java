package org.tbk.electrum.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Optional;

@Value
@Builder
@Jacksonized
public class WalletParams {
    @NonNull
    @JsonProperty("wallet_path")
    String walletPath;

    @JsonIgnore
    String password;

    @JsonIgnore
    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }
}
