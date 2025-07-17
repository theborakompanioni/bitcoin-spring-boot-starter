package org.tbk.bitcoin.regtest.electrum.common;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Optional;

@Value
@Builder
public class WalletParams {
    @NonNull
    String walletPath;

    String password;

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }
}
