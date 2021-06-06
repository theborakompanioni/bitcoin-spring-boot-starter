package org.tbk.bitcoin.tool.fee.mempoolspace;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tbk.bitcoin.tool.fee.FeeRecommendationRequest;
import org.tbk.bitcoin.tool.fee.FeeRecommendationRequestImpl;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponse;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ProjectedBlocksMempoolspaceFeeProviderTest {
    private static final String BASE_URL = "https://mempool.space";
    private static final String API_TOKEN = null;

    private ProjectedBlocksMempoolspaceFeeProvider sut;

    @BeforeEach
    void setUp() {
        MempoolspaceFeeApiClientImpl mempoolspaceFeeApiClient = new MempoolspaceFeeApiClientImpl(BASE_URL, API_TOKEN);
        this.sut = new ProjectedBlocksMempoolspaceFeeProvider(mempoolspaceFeeApiClient, Duration.ofSeconds(60));
    }

    @Test
    void itShouldNotSupportDurationTargetOverEightyMinutes() {
        FeeRecommendationRequest request = FeeRecommendationRequestImpl.builder()
                .durationTarget(Duration.ofMinutes(90))
                .build();

        assertThat(this.sut.supports(request), is(false));

        FeeRecommendationResponse feeRecommendationResponse = this.sut.request(request).blockFirst();

        assertThat(feeRecommendationResponse, is(nullValue()));
    }

    @Test
    void itShouldGetFeesRecommendation() {
        // we cannot ensure that mempool.space block projection based fee recommendations
        // have more than 1 block -> so at least just ensure that first block estimates are present
        List<Duration> durations = IntStream.range(0, 10)
                .mapToObj(Duration::ofMinutes)
                .collect(Collectors.toList());

        List<FeeRecommendationRequestImpl> requests = durations.stream()
                .map(it -> FeeRecommendationRequestImpl.builder()
                        .durationTarget(it)
                        .build())
                .collect(Collectors.toList());

        for (FeeRecommendationRequest request : requests) {
            FeeRecommendationResponse response = this.sut.request(request)
                    .blockFirst(Duration.ofSeconds(60));

            assertThat(response, is(notNullValue()));
            assertThat(response.getFeeRecommendations(), hasSize(1));
        }
    }
}
