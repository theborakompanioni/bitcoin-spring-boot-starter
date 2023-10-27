package org.tbk.bitcoin.tool.fee.blockstreaminfo;

import org.tbk.bitcoin.tool.fee.blockstreaminfo.proto.FeeEstimates;

public interface BlockstreamInfoFeeApiClient {
    FeeEstimates feeEstimates();
}
