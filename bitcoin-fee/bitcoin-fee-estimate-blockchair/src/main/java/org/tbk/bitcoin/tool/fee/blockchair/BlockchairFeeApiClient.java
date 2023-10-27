package org.tbk.bitcoin.tool.fee.blockchair;

import org.tbk.bitcoin.tool.fee.blockchair.proto.BitcoinStatsFeesOnly;

public interface BlockchairFeeApiClient {
    BitcoinStatsFeesOnly bitcoinStatsFeesOnly();
}
