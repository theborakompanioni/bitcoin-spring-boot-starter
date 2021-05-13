package org.tbk.bitcoin.regtest.mining;

import org.bitcoinj.core.Address;

import java.util.function.Supplier;

public interface CoinbaseRewardAddressSupplier extends Supplier<Address> {
}

