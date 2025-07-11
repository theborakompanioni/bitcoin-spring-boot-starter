package org.tbk.electrum.model;

import java.util.Optional;

public interface AddressWithBalance {
    String getAddress();

    TxoValue getBalance();

    Optional<String> getLabel();
}
