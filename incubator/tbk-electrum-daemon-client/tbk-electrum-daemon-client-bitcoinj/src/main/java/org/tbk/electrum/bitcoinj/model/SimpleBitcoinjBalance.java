package org.tbk.electrum.bitcoinj.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.bitcoinj.core.Coin;
import org.tbk.electrum.model.Balance;

import static org.tbk.electrum.bitcoinj.BitcoinjHelper.toCoin;

@Value
@Builder
public class SimpleBitcoinjBalance implements BitcoinjBalance {

    @NonNull
    Balance balance;

    @Override
    public Coin getConfirmed() {
        return toCoin(balance.getConfirmed());
    }

    @Override
    public Coin getUnconfirmed() {
        return toCoin(balance.getUnconfirmed());
    }

    @Override
    public Coin getUnmatured() {
        return toCoin(balance.getUnmatured());
    }

    @Override
    public Coin getTotal() {
        return toCoin(balance.getTotal());
    }

    @Override
    public Coin getSpendable() {
        return toCoin(balance.getSpendable());
    }
}
