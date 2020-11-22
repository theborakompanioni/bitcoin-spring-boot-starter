package org.tbk.bitcoin.fee.example.api;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbk.bitcoin.fee.example.api.proto.FeeRecommendation;
import org.tbk.bitcoin.fee.example.api.proto.FeeTableResponse;
import org.tbk.bitcoin.tool.fee.FeeProvider;
import org.tbk.bitcoin.tool.fee.FeeRecommendationRequest;
import org.tbk.bitcoin.tool.fee.FeeRecommendationRequestImpl;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/fee")
@RequiredArgsConstructor
public class FeeCtrl {
    private static final String SINGLE_KEY_VALUE = "*";
    private static final JsonFormat.Printer jsonPrinter = JsonFormat.printer();

    private final FeeProvider feeProvider;

    private final LoadingCache<String, FeeTableResponse> tableResponseCache = CacheBuilder.newBuilder()
            .refreshAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public FeeTableResponse load(String key) {
                    checkArgument(SINGLE_KEY_VALUE.equals(key));
                    return create();
                }
            });

    @GetMapping("/table")
    public ResponseEntity<FeeTableResponse> table() {

        FeeTableResponse feeTableResponse = tableResponseCache.getUnchecked(SINGLE_KEY_VALUE);


        return ResponseEntity.ok(feeTableResponse);
    }

    @GetMapping("/table.json")
    public ResponseEntity<String> tableJson() throws InvalidProtocolBufferException {

        FeeTableResponse feeTableResponse = tableResponseCache.getUnchecked(SINGLE_KEY_VALUE);

        String json = jsonPrinter.print(feeTableResponse);

        return ResponseEntity.ok(json);
    }


    private FeeTableResponse create() {
        List<Duration> durations = ImmutableList.<Duration>builder()
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

        List<FeeRecommendationRequest> requests = durations.stream()
                .map(val -> FeeRecommendationRequestImpl.builder()
                        .durationTarget(val)
                        .build())
                .collect(Collectors.toList());

        Map<Duration, List<FeeRecommendationResponse>> durationToFeeRecommendations = requests.stream()
                .collect(Collectors.toMap(FeeRecommendationRequest::getDurationTarget, val -> feeProvider.request(val)
                        .onErrorContinue((throwable, o) -> {
                            log.warn("", throwable);
                        })
                        .collectList()
                        .blockOptional(Duration.ofSeconds(30))
                        .orElseGet(Collections::emptyList)));


        Map<Duration, BigDecimal> values = durationToFeeRecommendations.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> {

                    DoubleSummaryStatistics feeSummaryStatistics = entry.getValue().stream()
                            .map(FeeRecommendationResponse::getFeeRecommendations)
                            .flatMap(Collection::stream)
                            .map(FeeRecommendationResponse.FeeRecommendation::getFeeUnit)
                            .mapToDouble(d -> d.getValue().doubleValue())
                            .summaryStatistics();

                    return BigDecimal.valueOf(feeSummaryStatistics.getAverage());
                }));

        return FeeTableResponse.newBuilder()
                .addColumn(FeeTableResponse.Column.newBuilder()
                        .setText("(without conf value)")
                        .build())
                .addAllRow(values.entrySet().stream()
                        .sorted(Comparator.comparingLong(val -> val.getKey().toMillis()))
                        .map(val -> FeeTableResponse.Row.newBuilder()
                                .setHeader(FeeTableResponse.Row.RowHeader.newBuilder()
                                        .setText(val.getKey().toString())
                                        .build())
                                .addEntry(FeeTableResponse.Row.RowEntry.newBuilder()
                                        .setText("" + val.getValue().setScale(2, RoundingMode.UP))
                                        .setValue(val.getValue().doubleValue())
                                        .addAllFeeRecommendation(durationToFeeRecommendations.get(val.getKey()).stream()
                                                .map(r -> FeeRecommendation.newBuilder()
                                                        .setValue(r.getFeeRecommendations().stream()
                                                                .map(r2 -> r2.getFeeUnit().getValue().doubleValue())
                                                                .findFirst().orElse(0d))
                                                        .setProviderName(r.getProviderInfo().getName())
                                                        .build())
                                                .collect(Collectors.toList())
                                        )
                                        .build())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
