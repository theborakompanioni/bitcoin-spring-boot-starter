package org.tbk.bitcoin.tool.fee.earndotcom.provider;

import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.FeeRecommendationImpl;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl;
import org.tbk.bitcoin.tool.fee.earndotcom.client.FeesSummaryEntry;
import org.tbk.bitcoin.tool.fee.earndotcom.client.TransactionFeesSummary;
import org.tbk.bitcoin.tool.fee.earndotcom.client.EarndotcomApiClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
public class EarndotcomFeeProvider extends AbstractFeeProvider {

    private static final ProviderInfo providerInfo = ProviderInfo.SimpleProviderInfo.builder()
            .name("earn.com")
            .description("")
            .build();

    private final EarndotcomApiClient client;

    private final FeeSelectionStrategy feeSelectionStrategy;

    public EarndotcomFeeProvider(EarndotcomApiClient client, FeeSelectionStrategy feeSelectionStrategy) {
        super(providerInfo);

        this.client = requireNonNull(client);
        this.feeSelectionStrategy = requireNonNull(feeSelectionStrategy);
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        return request.getDesiredConfidence().isEmpty();
    }

    @Override
    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {

        TransactionFeesSummary transactionFeesSummary = this.client.transactionFeesSummary();
        Optional<FeesSummaryEntry> summaryEntryOrEmpty = feeSelectionStrategy.select(request, transactionFeesSummary);

        if (summaryEntryOrEmpty.isEmpty()) {
            log.warn("no suitable estimation entries present in response for request: {}", request);
            return Flux.empty();
        }

        FeesSummaryEntry feesSummaryEntry = summaryEntryOrEmpty.orElseThrow();

        final BigDecimal satPerVbyte;
        if (request.isTargetDurationZeroOrLess()) {
            // take the maximum fee other pay if the requested amount is the special value zero
            satPerVbyte = BigDecimal.valueOf((feesSummaryEntry.getMinFee() + feesSummaryEntry.getMaxFee()))
                    .divide(BigDecimal.valueOf(2), 1, RoundingMode.HALF_UP);
        } else {
            // otherwise take the min from the eligible entries
            satPerVbyte = BigDecimal.valueOf(feesSummaryEntry.getMinFee());
        }

        return Flux.just(FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationImpl.builder()
                        .feeUnit(SatPerVbyteImpl.builder()
                                .satPerVbyteValue(satPerVbyte)
                                .build())
                        .build())
                .build());
    }
}
