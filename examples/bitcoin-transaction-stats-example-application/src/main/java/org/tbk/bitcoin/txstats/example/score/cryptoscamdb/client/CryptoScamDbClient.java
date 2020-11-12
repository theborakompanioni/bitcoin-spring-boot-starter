package org.tbk.bitcoin.txstats.example.score.cryptoscamdb.client;

public interface CryptoScamDbClient {

    CheckResponseDto check(String address);

    AddressesResponseDto addresses();
}
