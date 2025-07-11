package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SimpleAddressWithBalance implements AddressWithBalance {

    @NonNull
    String address;

    @NonNull
    TxoValue balance;
}
