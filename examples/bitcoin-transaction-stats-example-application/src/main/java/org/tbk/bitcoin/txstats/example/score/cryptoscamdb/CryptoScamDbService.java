package org.tbk.bitcoin.txstats.example.score.cryptoscamdb;

import org.tbk.bitcoin.txstats.example.score.cryptoscamdb.client.EntryDto;

import java.util.List;

public interface CryptoScamDbService {
    List<EntryDto> findMetaInfoOfAddress(String address);
}
