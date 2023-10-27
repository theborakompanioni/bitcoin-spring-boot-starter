package org.tbk.bitcoin.tool.fee.mempoolspace;

import org.tbk.bitcoin.tool.fee.mempoolspace.proto.FeesRecommended;
import org.tbk.bitcoin.tool.fee.mempoolspace.proto.ProjectedMempoolBlocks;

public interface MempoolspaceFeeApiClient {
    FeesRecommended feesRecommended();

    ProjectedMempoolBlocks projectedBlocks();
}
