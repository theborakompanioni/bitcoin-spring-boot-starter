package org.tbk.bitcoin.fee.example.internal.api;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbk.bitcoin.fee.example.internal.api.proto.FeeRecommendation;
import org.tbk.bitcoin.fee.example.internal.api.proto.FeeTableResponse;
import org.tbk.bitcoin.fee.example.internal.api.proto.FeeTableResponse.Row.RowEntry;
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

@Slf4j
@RestController
@RequestMapping(value = "/internal/api/v1/fee", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class FeeTableCtrl {
    private static final String FEE_TABLE_KEY = "FEE_TABLE";
    private static final String PROVIDER_FEE_TABLE_KEY = "PROVIDER_FEE_TABLE";

    private final FeeProvider feeProvider;
    private final JsonFormat.Printer jsonPrinter;

    private final LoadingCache<String, FeeTableResponse> tableResponseCache = CacheBuilder.newBuilder()
            .refreshAfterWrite(60, TimeUnit.SECONDS)
            .build(new CacheLoader<>() {
                @Override
                public FeeTableResponse load(String key) {
                    if (FEE_TABLE_KEY.equals(key)) {
                        return createFeeTable();
                    }
                    if (PROVIDER_FEE_TABLE_KEY.equals(key)) {
                        FeeTableResponse feeTableResponse = tableResponseCache.getUnchecked(FEE_TABLE_KEY);
                        return toProviderFeeTable(feeTableResponse);
                    }
                    throw new IllegalArgumentException("Key not supported: " + key);
                }
            });

    @GetMapping("/table.json")
    public ResponseEntity<String> tableJson() throws InvalidProtocolBufferException {

        FeeTableResponse feeTableResponse = tableResponseCache.getUnchecked(FEE_TABLE_KEY);

        String json = jsonPrinter.print(feeTableResponse);

        return ResponseEntity.ok(json);
    }


    @GetMapping("/provider/table.json")
    public ResponseEntity<String> providerTableJson() throws InvalidProtocolBufferException {

        FeeTableResponse providerFeeTable = tableResponseCache.getUnchecked(PROVIDER_FEE_TABLE_KEY);

        String json = jsonPrinter.print(providerFeeTable);

        return ResponseEntity.ok(json);
    }


    private FeeTableResponse createFeeTable() {
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


        Map<Duration, BigDecimal> averageValues = durationToFeeRecommendations.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    DoubleSummaryStatistics feeSummaryStatistics = entry.getValue().stream()
                            .map(FeeRecommendationResponse::getFeeRecommendations)
                            .flatMap(Collection::stream)
                            .map(FeeRecommendationResponse.FeeRecommendation::getFeeUnit)
                            .mapToDouble(d -> d.getValue().doubleValue())
                            .summaryStatistics();

                    return BigDecimal.valueOf(feeSummaryStatistics.getAverage());
                }));

        // as not all providers will return results for every duration, the average values per duration
        // might container higher values for longer durations - this should be prevented
        ImmutableMap.Builder<Duration, BigDecimal> rectifiedAverageValuesBuilder = ImmutableMap.builder();
        BigDecimal lastValue = averageValues.get(Duration.ZERO);
        for (Duration duration : durations) {
            BigDecimal currentValue = averageValues.get(duration);
            if (currentValue.compareTo(lastValue) > 0) {
                rectifiedAverageValuesBuilder.put(duration, lastValue);
            } else {
                rectifiedAverageValuesBuilder.put(duration, currentValue);
                lastValue = currentValue;
            }
        }
        Map<Duration, BigDecimal> rectifiedAverageValues = rectifiedAverageValuesBuilder.build();

        return FeeTableResponse.newBuilder()
                .addColumn(FeeTableResponse.Column.newBuilder()
                        .setText("(without conf value)")
                        .build())
                .addAllRow(rectifiedAverageValues.entrySet().stream()
                        .sorted(Comparator.comparingLong(val -> val.getKey().toMillis()))
                        .map(val -> FeeTableResponse.Row.newBuilder()
                                .setHeader(FeeTableResponse.Row.RowHeader.newBuilder()
                                        .setText(val.getKey().toString())
                                        .build())
                                .addEntry(RowEntry.newBuilder()
                                        .setText("" + val.getValue().setScale(2, RoundingMode.UP).toPlainString())
                                        .setValue(val.getValue().doubleValue())
                                        .addAllFeeRecommendation(durationToFeeRecommendations.get(val.getKey()).stream()
                                                .map(r -> FeeRecommendation.newBuilder()
                                                        .setValue(r.getFeeRecommendations().stream()
                                                                .map(r2 -> r2.getFeeUnit().getValue().doubleValue())
                                                                .findFirst().orElse(0d))
                                                        .setProviderName(r.getProviderInfo().getName())
                                                        .build())
                                                .toList()
                                        )
                                        .build())
                                .build())
                        .toList())
                .build();
    }


    public FeeTableResponse toProviderFeeTable(FeeTableResponse feeTable) {
        Set<String> providerNames = feeTable.getRowList().stream()
                .map(FeeTableResponse.Row::getEntryList)
                .flatMap(Collection::stream)
                .map(RowEntry::getFeeRecommendationList)
                .flatMap(Collection::stream)
                .map(FeeRecommendation::getProviderName)
                .collect(Collectors.toUnmodifiableSet());

        List<FeeTableResponse.Column> columns = providerNames.stream()
                .map(val -> FeeTableResponse.Column.newBuilder()
                        .setText(val)
                        .build())
                .toList();

        RowEntry missingEstimationRowEntry = RowEntry.newBuilder()
                .setText("-")
                .setValue(1d)
                .build();

        List<FeeTableResponse.Row> rows = feeTable.getRowList().stream()
                .map(row -> {
                    List<RowEntry> rowEntries = row.getEntryList().stream()
                            .flatMap(entry -> providerNames.stream()
                                    .map(provider -> entry.getFeeRecommendationList().stream()
                                            .filter(rec -> provider.equals(rec.getProviderName()))
                                            .findFirst()
                                            .map(recommendation -> RowEntry.newBuilder()
                                                    .setText("" + BigDecimal.valueOf(recommendation.getValue()).setScale(2, RoundingMode.UP).toPlainString())
                                                    .setValue(recommendation.getValue())
                                                    .addFeeRecommendation(recommendation)
                                                    .build()
                                            )
                                            .orElse(missingEstimationRowEntry)))
                            .toList();

                    return FeeTableResponse.Row.newBuilder()
                            .setHeader(row.getHeader())
                            .addAllEntry(rowEntries)
                            .build();
                })
                .toList();

        return FeeTableResponse.newBuilder()
                .addAllColumn(columns)
                .addAllRow(rows)
                .build();
    }

}
