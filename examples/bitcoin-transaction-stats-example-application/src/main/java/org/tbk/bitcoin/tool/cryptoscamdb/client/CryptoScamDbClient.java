package org.tbk.bitcoin.tool.cryptoscamdb.client;

public interface CryptoScamDbClient {

    CheckResponseDto check(String address);

    AddressesResponseDto addresses();
}
