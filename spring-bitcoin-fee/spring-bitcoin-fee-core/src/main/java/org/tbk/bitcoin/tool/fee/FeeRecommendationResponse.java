package org.tbk.bitcoin.tool.fee;

import java.util.List;

public interface FeeRecommendationResponse {

    List<FeeRecommendation> getFeeRecommendations();

    interface FeeRecommendation {
        SatPerVbyte getSatPerVbyte();
    }

    interface SatPerVbyte {
        long getSatPerVbyteValue();
    }
}
