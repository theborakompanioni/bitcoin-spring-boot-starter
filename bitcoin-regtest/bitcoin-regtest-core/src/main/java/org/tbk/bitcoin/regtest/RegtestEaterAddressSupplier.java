package org.tbk.bitcoin.regtest;

import org.bitcoinj.core.Address;
import org.bitcoinj.params.RegTestParams;

public final class RegtestEaterAddressSupplier implements CoinbaseRewardAddressSupplier {
    // an address not controlled by the bitcoin core testcontainer (taken from second_wallet in electrum module)
    // replace with "real" eater address aka "bcrt1b1tco1neaterdntsend1111111xqc4j" but with valid checksum
    private static final Address regtestEaterAddress = Address.fromString(RegTestParams.get(), "bcrt1q4m4fds2rdtgde67ws5aema2a2wqvv7uzyxqc4j");

    @Override
    public Address get() {
        return regtestEaterAddress;
    }
}
