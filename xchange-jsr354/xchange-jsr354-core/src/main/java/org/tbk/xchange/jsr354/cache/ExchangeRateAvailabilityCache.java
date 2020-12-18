package org.tbk.xchange.jsr354.cache;

import com.google.common.cache.ForwardingLoadingCache.SimpleForwardingLoadingCache;
import lombok.extern.slf4j.Slf4j;

import javax.money.convert.ConversionQuery;

@Slf4j
public final class ExchangeRateAvailabilityCache extends SimpleForwardingLoadingCache<ConversionQuery, Boolean> {

    public ExchangeRateAvailabilityCache(ConversionQueryCache.Builder<Boolean> builder) {
        super(builder.build((provider, conversionQuery) -> {
            log.debug("loading exchange rate availability value for {} from provider {}", conversionQuery, provider);

            boolean available = provider.isAvailable(conversionQuery);

            log.debug("loaded exchange rate availability value for {} from provider {}: {}", conversionQuery, provider, available);

            return available;
        }));
    }
}
