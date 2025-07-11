package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Optional;

@Value
@Builder
public class SimpleAddressWithBalance implements AddressWithBalance {

    @NonNull
    String address;

    @NonNull
    TxoValue balance;

    String label;

    public Optional<String> getLabel() {
        return Optional.ofNullable(label);
    }
}
