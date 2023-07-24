package org.tbk.bitcoin.regtest.mining;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bitcoinj.core.Address;

import static java.util.Objects.requireNonNull;

@SuppressFBWarnings(value = {"EI_EXPOSE_REP"}, justification = "on purpose")
public final class StaticCoinbaseRewardAddressSupplier implements CoinbaseRewardAddressSupplier {

    private final Address address;
    
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "class from external dependency")
    public StaticCoinbaseRewardAddressSupplier(Address client) {
        this.address = requireNonNull(client);
    }

    @Override
    public Address get() {
        return this.address;
    }
}
