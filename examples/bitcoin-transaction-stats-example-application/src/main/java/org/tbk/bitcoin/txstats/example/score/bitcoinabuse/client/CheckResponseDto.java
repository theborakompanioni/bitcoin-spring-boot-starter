package org.tbk.bitcoin.txstats.example.score.bitcoinabuse.client;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;


// e.g. see response https://www.bitcoinabuse.com/api/reports/check?address=1KCuTx8TZ4buoXoh9UaPhA4WhZttFwhtbS&api_token=
@Value
@Builder
@Jacksonized
public class CheckResponseDto {
    String address;

    long count;
}
