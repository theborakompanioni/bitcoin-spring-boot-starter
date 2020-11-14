package org.tbk.bitcoin.txstats.example.score.bitcoinabuse;

import java.util.List;
import java.util.Map;

public interface BtcAbuseService {
    List<Map<String, Object>> findMetaInfoOfAddress(String address);
}
