package org.tbk.bitcoin.fee.example.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.util.MoreBitcoin;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/fee", produces = MediaType.APPLICATION_JSON_VALUE)
public class FeeCtrl {
    private static final Duration defaultDuration = Duration.ofMinutes(30);

    private static final List<Duration> durations = ImmutableList.<Duration>builder()
            .add(Duration.ZERO)
            .add(Duration.ofMinutes(10))
            .add(Duration.ofMinutes(20))
            .add(Duration.ofMinutes(30))
            .add(Duration.ofMinutes(60))
            .add(Duration.ofHours(2))
            .add(Duration.ofHours(3))
            .add(Duration.ofHours(6))
            .add(Duration.ofHours(12))
            .add(Duration.ofHours(24))
            .build();

    private final List<FeeProvider> feeProviders;

    private final FeeProvider primaryFeeProvider;

    public FeeCtrl(List<FeeProvider> feeProviders, FeeProvider primaryFeeProvider) {
        this.feeProviders = List.copyOf(requireNonNull(feeProviders));
        this.primaryFeeProvider = requireNonNull(primaryFeeProvider);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<Map<Duration, List<FeeRecommendationResponse>>> recommendations() {

        List<FeeRecommendationRequest> requests = durations.stream()
                .map(val -> FeeRecommendationRequestImpl.builder()
                        .durationTarget(val)
                        .build())
                .collect(Collectors.toList());

        Map<Duration, List<FeeRecommendationResponse>> durationToFeeRecommendations = requests.stream()
                .collect(Collectors.toMap(FeeRecommendationRequest::getDurationTarget, val -> primaryFeeProvider.request(val)
                        .onErrorContinue((throwable, o) -> {
                            log.warn("", throwable);
                        })
                        .collectList()
                        .blockOptional(Duration.ofSeconds(15))
                        .orElseGet(Collections::emptyList)));

        return ResponseEntity.ok(durationToFeeRecommendations);
    }

    @GetMapping("/recommendation")
    public ResponseEntity<FeeRecommendationResponseDto> recommendation(
            @RequestParam(name = "block_target", required = false) Integer blockTarget
    ) {
        Duration duration = Optional.ofNullable(blockTarget)
                .filter(it -> it >= 0)
                .map(MoreBitcoin::averageBlockDuration)
                .orElse(defaultDuration);

        FeeRecommendationRequestImpl request = FeeRecommendationRequestImpl.builder()
                .durationTarget(duration)
                .build();

        List<FeeRecommendationResponse> feeRecommendationResponses = primaryFeeProvider.request(request)
                .onErrorContinue((throwable, o) -> {
                    log.warn("", throwable);
                })
                .collectList()
                .blockOptional(Duration.ofSeconds(30))
                .orElseGet(Collections::emptyList);

        return ResponseEntity.ok(FeeRecommendationResponseDto.builder()
                .recommendations(feeRecommendationResponses)
                .build());
    }

    @GetMapping("/provider")
    public ResponseEntity<ProviderResponseDto> provider() {
        List<ProviderInfo> providerInfos = feeProviders.stream()
                .map(FeeProvider::info)
                .toList();

        return ResponseEntity.ok(ProviderResponseDto.builder()
                .primaryProvider(primaryFeeProvider.info())
                .providers(providerInfos)
                .build());
    }

    @Value
    @Builder
    @Jacksonized
    public static class FeeRecommendationResponseDto {
        @JsonProperty("recommendations")
        List<FeeRecommendationResponse> recommendations;
    }

    @Value
    @Builder
    @Jacksonized
    public static class ProviderResponseDto {
        @JsonProperty("primary_provider")
        ProviderInfo primaryProvider;

        @JsonProperty("providers")
        @Singular("addProvider")
        List<ProviderInfo> providers;
    }
}
