package org.tbk.bitcoin.regtest;

import org.bitcoinj.core.Address;

import java.util.function.Supplier;

public interface CoinbaseRewardAddressSupplier extends Supplier<Address> {
}

