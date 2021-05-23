package org.tbk.bitcoin.regtest.electrum.faucet;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.tbk.bitcoin.regtest.common.AddressSupplier;
import reactor.core.publisher.Mono;

public interface ElectrumRegtestFaucet {

    Mono<Sha256Hash> requestBitcoin(AddressSupplier address, Coin amount);

}
