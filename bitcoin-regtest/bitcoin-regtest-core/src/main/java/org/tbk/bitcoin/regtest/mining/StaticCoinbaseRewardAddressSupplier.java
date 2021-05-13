package org.tbk.bitcoin.regtest.mining;

import org.bitcoinj.core.Address;

import static java.util.Objects.requireNonNull;

public final class StaticCoinbaseRewardAddressSupplier implements CoinbaseRewardAddressSupplier {

    private final Address address;

    public StaticCoinbaseRewardAddressSupplier(Address client) {
        this.address = requireNonNull(client);
    }

    @Override
    public Address get() {
        return this.address;
    }
}

