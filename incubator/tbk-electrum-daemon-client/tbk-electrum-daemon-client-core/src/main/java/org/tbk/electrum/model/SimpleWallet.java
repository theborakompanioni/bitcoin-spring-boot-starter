package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SimpleWallet implements Wallet {
    @NonNull
    Seed seed;

    @NonNull
    String path;
}
