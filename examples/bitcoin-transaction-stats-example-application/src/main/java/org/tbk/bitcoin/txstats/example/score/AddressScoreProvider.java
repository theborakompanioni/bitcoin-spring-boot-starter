package org.tbk.bitcoin.txstats.example.score;

import java.util.List;

public interface AddressScoreProvider {

    List<AddressScoreAnalysis> gradeAddress(AddressScoreInput input);
}
