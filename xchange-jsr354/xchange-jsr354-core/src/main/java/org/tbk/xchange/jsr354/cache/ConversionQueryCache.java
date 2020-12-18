package org.tbk.xchange.jsr354.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.ForwardingLoadingCache.SimpleForwardingLoadingCache;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import javax.money.convert.ConversionQuery;
import javax.money.convert.ExchangeRateProvider;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
public class ConversionQueryCache<T> extends SimpleForwardingLoadingCache<ConversionQuery, T> {

    public interface Builder<T> {
        ConversionQueryCache<T> build(BiFunction<ExchangeRateProvider, ConversionQuery, T> mapper);
    }

    private static boolean hasExpireOrRefreshSpecified(CacheBuilderSpec cacheBuilderSpec) {
        String cacheSpec = cacheBuilderSpec.toParsableString();

        return cacheSpec.contains("expireAfterAccess") ||
                cacheSpec.contains("expireAfterWrite") ||
                cacheSpec.contains("refreshAfterWrite") ||
                cacheSpec.contains("refreshInterval");
    }

    public static <T> Builder<T> builder(CacheBuilderSpec cacheBuilderSpec,
                                         ExchangeRateProvider provider) {
        checkArgument(hasExpireOrRefreshSpecified(cacheBuilderSpec), "'cacheBuilderSpec' spec must have expire or refresh configured");

        return mapper -> new ConversionQueryCache<T>(CacheBuilder.from(cacheBuilderSpec)
                .removalListener(notification -> {
                    log.debug("remove from cache because of {}: {}->{}",
                            notification.getCause(), notification.getKey(), notification.getValue());
                })
                .build(new CacheLoader<>() {
                    @Override
                    public T load(ConversionQuery conversionQuery) {
                        return mapper.apply(provider, conversionQuery);
                    }
                }));
    }

    private ConversionQueryCache(LoadingCache<ConversionQuery, T> delegate) {
        super(delegate);
    }
}
