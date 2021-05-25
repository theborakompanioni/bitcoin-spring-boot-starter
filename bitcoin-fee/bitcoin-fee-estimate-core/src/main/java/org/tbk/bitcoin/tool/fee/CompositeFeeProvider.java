package org.tbk.bitcoin.tool.fee;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public final class CompositeFeeProvider extends AbstractFeeProvider {

    private final List<FeeProvider> feeProviders;

    private final ProviderInfo providerInfo;
    private final Scheduler scheduler;

    public CompositeFeeProvider(List<FeeProvider> feeProviders) {
        this(feeProviders, Schedulers.parallel());
    }

    public CompositeFeeProvider(List<FeeProvider> feeProviders, Scheduler scheduler) {
        this.feeProviders = ImmutableList.copyOf(requireNonNull(feeProviders));
        this.scheduler = requireNonNull(scheduler);
        this.providerInfo = ProviderInfo.SimpleProviderInfo.builder()
                .name("composite")
                .description(toDescription(this.feeProviders))
                .build();
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        return this.feeProviders.stream().anyMatch(provider -> provider.supports(request));
    }

    @Override
    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        return Flux.fromIterable(feeProviders)
                .filter(provider -> provider.supports(request))
                .parallel().runOn(this.scheduler)
                .flatMap(provider -> provider.request(request))
                .sequential();
    }

    @Override
    protected ProviderInfo infoHook() {
        return providerInfo;
    }

    @VisibleForTesting
    int getProviderCount() {
        return feeProviders.size();
    }

    private static String toDescription(List<FeeProvider> feeProviders) {
        String commaSeparatedProviderNamesOrEmpty = feeProviders.stream()
                .map(FeeProvider::info)
                .map(ProviderInfo::getName)
                .collect(Collectors.joining(", "));

        String commaSeparatedProviderNames = Optional.of(commaSeparatedProviderNamesOrEmpty)
                .filter(it -> !it.isBlank())
                .orElse("<empty>");

        return String.format("A composite fee provider backed by %d providers: %s", feeProviders.size(), commaSeparatedProviderNames);
    }
}
