package org.tbk.bitcoin.regtest.electrum.faucet;

import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;
import org.tbk.bitcoin.regtest.common.AddressSupplier;
import reactor.core.publisher.Mono;

public interface ElectrumRegtestFaucet {

    Mono<Sha256Hash> requestBitcoin(AddressSupplier address, Coin amount);

}
