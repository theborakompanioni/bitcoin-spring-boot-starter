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
import javax.money.convert.*;
import java.math.MathContext;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

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

        Currency targetCurrency = Currency.getInstance(targetCurrencyUnit.getCurrencyCode());
        Currency baseCurrency = Currency.getInstance(baseCurrencyUnit.getCurrencyCode());

        ConversionContext conversionContext = ConversionContext.from(this.getContext(), RateType.DEFERRED);

        try {
            CurrencyPair currencyPair = new CurrencyPair(baseCurrency, targetCurrency);

            ExchangeRateBuilder exchangeRateBuilder = new ExchangeRateBuilder(conversionContext)
                    .setBase(baseCurrencyUnit)
                    .setTerm(targetCurrencyUnit);

            // this is done like in the ECB provider -> reverse the rate if not found directly..
            // TODO: do it more like IMFAbstractRateProvider -> always try to convert to BTC
            // then also EUR to USD conversion is possible!
            if (isConversionAvailable(currencyPair)) {
                Ticker ticker = cache.get(currencyPair);

                return exchangeRateBuilder
                        .setFactor(DefaultNumberValue.of(ticker.getAsk()))
                        .build();
            }

            CurrencyPair currencyPairReverse = new CurrencyPair(targetCurrency, baseCurrency);
            if (isConversionAvailable(currencyPairReverse)) {
                Ticker ticker = cache.get(currencyPairReverse);

                return reverse(exchangeRateBuilder
                        .setFactor(DefaultNumberValue.of(ticker.getAsk()))
                        .build());
            }

            CurrencyPair btcToBase = new CurrencyPair(Currency.getInstance("BTC"), baseCurrency);
            CurrencyPair btcToTarget = new CurrencyPair(Currency.getInstance("BTC"), targetCurrency);
            if (isConversionAvailable(btcToBase) && isConversionAvailable(btcToTarget)) {

                Ticker tickerBtcToBase = cache.get(btcToBase);
                Ticker tickerBtcToTarget = cache.get(btcToTarget);

                // TODO: find common base of the two like in IMF provider
                //throw new ExecutionException(new IllegalStateException());
            }
        } catch (ExecutionException e) {
            log.warn("", e);
        }

        // no result - return null!
        return null;
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

}
