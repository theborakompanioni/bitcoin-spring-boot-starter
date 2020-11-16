package org.tbk.bitcoin.tool.fee;

import reactor.core.publisher.Flux;

import static java.util.Objects.requireNonNull;

public abstract class DelegatingFeeProvider extends AbstractFeeProvider {

    private final FeeProvider delegate;

    public DelegatingFeeProvider(FeeProvider delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        return this.delegate.supports(request);
    }

    @Override
    protected final Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        return this.delegate.request(request);
    }
}
