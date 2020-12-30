package org.tbk.spring.testcontainer.bitcoind.regtest;

import org.bitcoinj.core.Address;

import static java.util.Objects.requireNonNull;

public class StaticCoinbaseRewardAddressSupplier implements CoinbaseRewardAddressSupplier {

    private final Address address;

    public StaticCoinbaseRewardAddressSupplier(Address client) {
        this.address = requireNonNull(client);
    }

    @Override
    public Address get() {
        return this.address;
    }
}

