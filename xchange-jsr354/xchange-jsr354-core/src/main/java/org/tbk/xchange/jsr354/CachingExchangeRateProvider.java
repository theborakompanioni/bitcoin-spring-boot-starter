package org.tbk.xchange.jsr354;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.spi.AbstractRateProvider;
import org.javamoney.moneta.spi.LazyBoundCurrencyConversion;
import org.tbk.xchange.jsr354.cache.ExchangeRateAvailabilityCache;
import org.tbk.xchange.jsr354.cache.ExchangeRateCache;

import javax.money.convert.*;

import static java.util.Objects.requireNonNull;

@Slf4j
public class CachingExchangeRateProvider extends AbstractRateProvider {

    private final ExchangeRateAvailabilityCache availabilityCache;

    private final ExchangeRateCache exchangeRateCache;

    public CachingExchangeRateProvider(ProviderContext providerContext,
                                       ExchangeRateAvailabilityCache availabilityCache,
                                       ExchangeRateCache exchangeRateCache) {
        super(ProviderContextBuilder.create(providerContext)
                .setRateTypes(RateType.DEFERRED)
                .build());

        this.availabilityCache = requireNonNull(availabilityCache);
        this.exchangeRateCache = requireNonNull(exchangeRateCache);
    }

    @Override
    public boolean isAvailable(@NonNull ConversionQuery conversionQuery) {
        boolean realtimeExplicitlyDemanded = isRealtimeExplicitlyDemanded(conversionQuery);

        if (realtimeExplicitlyDemanded) {
            availabilityCache.refresh(conversionQuery);
        }

        return availabilityCache.getUnchecked(conversionQuery);
    }

    @Override
    public ExchangeRate getExchangeRate(ConversionQuery conversionQuery) {
        boolean realtimeExplicitlyDemanded = isRealtimeExplicitlyDemanded(conversionQuery);

        if (realtimeExplicitlyDemanded) {
            exchangeRateCache.refresh(conversionQuery);
        }

        return exchangeRateCache.getUnchecked(conversionQuery);
    }

    private boolean isRealtimeExplicitlyDemanded(ConversionQuery conversionQuery) {
        return conversionQuery.getRateTypes().size() == 1
                && conversionQuery.getRateTypes().contains(RateType.REALTIME);
    }

    @Override
    public CurrencyConversion getCurrencyConversion(ConversionQuery conversionQuery) {
        if (getContext().getRateTypes().size() == 1) {
            RateType rateType = getContext().getRateTypes().iterator().next();
            ConversionContext singleRateConversionContext = createConversionContextBuilder(rateType).build();
            return new LazyBoundCurrencyConversion(conversionQuery, this, singleRateConversionContext);
        }

        ConversionContext multiRateConversionContext = createConversionContextBuilder(RateType.ANY).build();
        return new LazyBoundCurrencyConversion(conversionQuery, this, multiRateConversionContext);
    }

    private ConversionContextBuilder createConversionContextBuilder(RateType type) {
        return ConversionContextBuilder.create(this.getContext(), type);
    }
}
