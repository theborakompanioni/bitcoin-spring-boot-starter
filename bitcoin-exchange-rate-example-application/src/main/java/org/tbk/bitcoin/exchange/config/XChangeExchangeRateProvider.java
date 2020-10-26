package org.tbk.bitcoin.exchange.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.convert.ExchangeRateBuilder;
import org.javamoney.moneta.spi.AbstractRateProvider;
import org.javamoney.moneta.spi.DefaultNumberValue;
import org.javamoney.moneta.spi.LazyBoundCurrencyConversion;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;

import javax.money.CurrencyUnit;
import javax.money.NumberValue;
import javax.money.convert.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
public class XChangeExchangeRateProvider extends AbstractRateProvider implements ExchangeRateProvider {

    private static final Duration defaultExpireAfterWriteDuration = Duration.ofSeconds(10);

    private final LoadingCache<CurrencyPair, Ticker> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(defaultExpireAfterWriteDuration)
            .build(new CacheLoader<>() {
                @Override
                public Ticker load(CurrencyPair currencyPair) throws Exception {
                    return exchange.getMarketDataService().getTicker(currencyPair);
                }
            });

    @NonNull
    private final Exchange exchange;

    public XChangeExchangeRateProvider(ProviderContext providerContext, Exchange exchange) {
        super(providerContext);
        this.exchange = exchange;
    }

    @Override
    public ExchangeRate getExchangeRate(ConversionQuery conversionQuery) {
        CurrencyUnit targetCurrencyUnit = conversionQuery.getCurrency();
        CurrencyUnit baseCurrencyUnit = conversionQuery.getBaseCurrency();

        Optional<ExchangeRate> exchangeRateOrEmpty = getExchangeRateIfAvailable(baseCurrencyUnit, targetCurrencyUnit)
                .or(() -> getExchangeRateIfAvailable(targetCurrencyUnit, baseCurrencyUnit).map(this::reverse));

        return exchangeRateOrEmpty.orElse(null);
    }

    private Optional<ExchangeRate> getExchangeRateIfAvailable(CurrencyUnit baseCurrencyUnit, CurrencyUnit targetCurrencyUnit) {
        try {
            Currency baseCurrency = Currency.getInstance(baseCurrencyUnit.getCurrencyCode());
            Currency targetCurrency = Currency.getInstance(targetCurrencyUnit.getCurrencyCode());

            CurrencyPair currencyPair = new CurrencyPair(baseCurrency, targetCurrency);
            if (isConversionAvailable(currencyPair)) {
                Ticker ticker = cache.get(currencyPair);

                ConversionContext conversionContext = createConversionContextFromTicker(ticker);

                Optional<NumberValue> exchangeRateValueOrEmpty = Optional.ofNullable(ticker.getLast())
                        .or(() -> Optional.ofNullable(ticker.getAsk()))
                        .or(() -> Optional.ofNullable(ticker.getBid()))
                        // filter zero as some exchanges return 0 when tey do not support the currency pair
                        .filter(val -> BigDecimal.ZERO.compareTo(val) != 0)
                        .map(DefaultNumberValue::of);

                if (exchangeRateValueOrEmpty.isPresent()) {
                    ExchangeRate exchangeRate = new ExchangeRateBuilder(conversionContext)
                            .setBase(baseCurrencyUnit)
                            .setTerm(targetCurrencyUnit)
                            .setFactor(exchangeRateValueOrEmpty.get())
                            .build();

                    return Optional.of(exchangeRate);
                }
            }
        } catch (Exception e) {
            log.warn("", e);
        }

        return Optional.empty();
    }

    private ConversionContext createConversionContextFromTicker(Ticker ticker) {
        ConversionContextBuilder builder = ConversionContextBuilder.create(this.getContext(), RateType.DEFERRED);
        TickerHelper.createInfoMap(ticker).forEach(builder::set);
        return builder.build();
    }

    private boolean isConversionAvailable(CurrencyPair currencyPair) {
        try {
            Ticker ticker = cache.get(currencyPair);
            return true;
        } catch (ExecutionException e) {
            return false;
        }
    }

    @Override
    public CurrencyConversion getCurrencyConversion(ConversionQuery conversionQuery) {
        if (getContext().getRateTypes().size() == 1) {
            return new LazyBoundCurrencyConversion(conversionQuery, this, ConversionContext
                    .of(getContext().getProviderName(), getContext().getRateTypes().iterator().next()));
        }
        return new LazyBoundCurrencyConversion(conversionQuery, this,
                ConversionContext.of(getContext().getProviderName(), RateType.ANY));
    }

    private ExchangeRate reverse(ExchangeRate rate) {
        requireNonNull(rate);
        return new ExchangeRateBuilder(rate).setRate(rate).setBase(rate.getCurrency()).setTerm(rate.getBaseCurrency())
                .setFactor(divide(DefaultNumberValue.ONE, rate.getFactor(), MathContext.DECIMAL64)).build();
    }

    private static final class TickerHelper {

        public static Map<String, Object> createInfoMap(Ticker ticker) {
            return createInfoMapWithNulls(ticker).entrySet().stream()
                    .filter(val -> val.getValue() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        private static Map<String, Object> createInfoMapWithNulls(Ticker ticker) {
            Map<String, Object> map = new HashMap<>(12, 1f);
            map.put("ask", ticker.getAsk());
            map.put("askSize", ticker.getAskSize());
            map.put("bid", ticker.getBid());
            map.put("bidSize", ticker.getBidSize());
            map.put("high", ticker.getHigh());
            map.put("last", ticker.getLast());
            map.put("low", ticker.getLow());
            map.put("open", ticker.getOpen());
            map.put("quoteVolume", ticker.getQuoteVolume());
            map.put("timestamp", ticker.getTimestamp());
            map.put("volume", ticker.getVolume());
            map.put("vwap", ticker.getVwap());
            return map;
        }
    }
}
