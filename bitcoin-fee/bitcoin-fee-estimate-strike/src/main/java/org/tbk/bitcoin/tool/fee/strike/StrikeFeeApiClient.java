package org.tbk.bitcoin.tool.fee.strike;

import org.tbk.bitcoin.tool.fee.strike.proto.BlendedFeeEstimateResponse;

public interface StrikeFeeApiClient {
    BlendedFeeEstimateResponse feeEstimates();
}
