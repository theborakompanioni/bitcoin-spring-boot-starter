package org.tbk.bitcoin.txstats.example.score.bitcoinabuse.client;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class AbuseType {

    long id;

    String label;
}
