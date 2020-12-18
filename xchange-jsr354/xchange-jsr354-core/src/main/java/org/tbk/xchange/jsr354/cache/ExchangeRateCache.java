package org.tbk.xchange.jsr354.cache;

import com.google.common.cache.ForwardingLoadingCache.SimpleForwardingLoadingCache;
import lombok.extern.slf4j.Slf4j;

import javax.money.convert.ConversionQuery;
import javax.money.convert.ExchangeRate;

@Slf4j
public final class ExchangeRateCache extends SimpleForwardingLoadingCache<ConversionQuery, ExchangeRate> {

    public ExchangeRateCache(ConversionQueryCache.Builder<ExchangeRate> builder) {
        super(builder.build((provider, conversionQuery) -> {
            log.debug("loading exchange rate value for {} from provider {}", conversionQuery, provider);

            ExchangeRate exchangeRate = provider.getExchangeRate(conversionQuery);

            log.debug("loaded exchange rate value for {} from provider {}: {}", conversionQuery, provider, exchangeRate);

            return exchangeRate;

        }));
    }
}
