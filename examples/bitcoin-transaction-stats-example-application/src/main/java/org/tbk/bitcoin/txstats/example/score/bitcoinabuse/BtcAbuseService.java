package org.tbk.bitcoin.txstats.example.score.bitcoinabuse;

import org.tbk.bitcoin.tool.btcabuse.CheckResponseDto;

import java.util.List;

public interface BtcAbuseService {
    List<CheckResponseDto> findMetaInfoOfAddress(String address);
}
