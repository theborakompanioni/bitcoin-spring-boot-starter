package org.tbk.electrum.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import javax.annotation.Nullable;

@Value
@Builder(toBuilder = true)
public class ListAddressParams {
    private static final ListAddressParams ALL = builder().build();

    /**
     * @deprecated use with wallet_path
     */
    @Deprecated
    public static ListAddressParams all() {
        return ALL;
    }

    public static ListAddressParams all(String walletPath) {
        return all().toBuilder()
                .walletPath(walletPath)
                .build();
    }

    @Nullable
    Boolean receiving;
    @Nullable
    Boolean change;
    @Nullable
    Boolean frozen;
    @Nullable
    Boolean unused;
    @Nullable
    Boolean funded;

    @JsonProperty("wallet_path")
    String walletPath;
}
