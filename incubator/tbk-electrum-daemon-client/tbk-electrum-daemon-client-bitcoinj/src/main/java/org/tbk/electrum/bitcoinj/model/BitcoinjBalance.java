package org.tbk.electrum.bitcoinj.model;

import org.bitcoinj.core.Coin;

public interface BitcoinjBalance {
    Coin getConfirmed();

    Coin getUnconfirmed();

    Coin getUnmatured();

    Coin getTotal();

    Coin getSpendable();
}
