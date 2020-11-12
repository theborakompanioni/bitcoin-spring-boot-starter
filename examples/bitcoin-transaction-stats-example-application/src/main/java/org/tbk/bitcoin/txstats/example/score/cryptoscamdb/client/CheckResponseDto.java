package org.tbk.bitcoin.txstats.example.score.cryptoscamdb.client;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;


// e.g. see response  https://api.cryptoscamdb.org/v1/check/16wd9B1LiXmTNf9hxQyb3Q9fbVHzP3NvSV
@Value
@Builder
@Jacksonized
public class CheckResponseDto {
    String input;

    boolean success;

    String coin; // e.g "BTC"

    Result result;

    @Value
    @Builder
    @Jacksonized
    public static class Result {
        String status; // e.g. "neutral" / "blocked"
        String type; // e.g. "address" / "ip"

        String coin; // e.g. "BTC" / "ETH"

        List<EntryDto> entries;
    }
}
