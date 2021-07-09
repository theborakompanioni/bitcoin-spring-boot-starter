package org.tbk.bitcoin.tool.fee;

import reactor.core.publisher.Flux;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public abstract class AbstractFeeProvider implements FeeProvider {

    private final ProviderInfo providerInfo;

    protected AbstractFeeProvider() {
        this.providerInfo = null;
    }

    protected AbstractFeeProvider(ProviderInfo providerInfo) {
        this.providerInfo = requireNonNull(providerInfo);
    }

    @Override
    public final ProviderInfo info() {
        return infoHook();
    }

    @Override
    public final Flux<FeeRecommendationResponse> request(FeeRecommendationRequest feeRecommendationRequest) {
        Flux<FeeRecommendationResponse> result = Flux.just(feeRecommendationRequest)
                .filter(this::supports)
                .flatMap(this::requestHook)
                .flatMap(this::transformHook)
                .map(val -> FeeRecommendationResponseImpl.builder()
                        .feeRecommendations(val.getFeeRecommendations())
                        .providerInfo(Optional.ofNullable(val.getProviderInfo()).orElseGet(this::info))
                        .build()
                );

        return reduceHook(result);
    }

    protected ProviderInfo infoHook() {
        return this.providerInfo;
    }

    protected abstract Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request);

    /**
     * A hook to transform a single {@link FeeRecommendationResponse} response.
     * Append information, filter, transform or add additional responses.
     *
     * <p>Default behaviour is to return the unchanged value that has been provided.
     *
     * @param response the fee recommendation response
     * @return the unchanged value that has been provided.
     */
    protected Flux<FeeRecommendationResponse> transformHook(FeeRecommendationResponse response) {
        return Flux.just(response);
    }

    /**
     * A hook to transform a stream of {@link FeeRecommendationResponse} responses.
     * This is the place to e.g. aggregate results or derive statistical metrics.
     *
     * <p>Default behaviour is to return the unchanged stream that has been provided.
     *
     * @param responses the fee recommendation responses
     * @return the unchanged stream that has been provided.
     */
    protected Flux<FeeRecommendationResponse> reduceHook(Flux<FeeRecommendationResponse> responses) {
        return responses;
    }
}
