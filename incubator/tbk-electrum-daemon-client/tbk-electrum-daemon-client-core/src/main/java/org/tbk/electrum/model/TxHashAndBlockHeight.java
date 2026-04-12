package org.tbk.electrum.model;

import java.util.Optional;

public interface TxHashAndBlockHeight {
    Optional<Long> getHeight();

    String getTxHash();

    boolean isConfirmed();

    boolean isInMempool();

    boolean isAllInputsConfirmed();
}
