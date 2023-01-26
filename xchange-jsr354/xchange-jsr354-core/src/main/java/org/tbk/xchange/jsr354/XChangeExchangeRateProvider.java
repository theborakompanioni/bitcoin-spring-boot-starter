package org.tbk.xchange.jsr354;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.convert.ExchangeRateBuilder;
import org.javamoney.moneta.spi.AbstractRateProvider;
import org.javamoney.moneta.spi.DefaultNumberValue;
import org.javamoney.moneta.spi.LazyBoundCurrencyConversion;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.instrument.Instrument;

import javax.money.CurrencyUnit;
import javax.money.NumberValue;
import javax.money.convert.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
public class XChangeExchangeRateProvider extends AbstractRateProvider {

    private final Exchange exchange;

    public XChangeExchangeRateProvider(ProviderContext providerContext, Exchange exchange) {
        super(providerContext);

        this.exchange = requireNonNull(exchange);
    }

    @Override
    public boolean isAvailable(@NonNull ConversionQuery conversionQuery) {
        try {
            Optional<CurrencyPair> currencyPairOrEmpty = MoreCurrencyPairs.toCurrencyPair(conversionQuery);

            return currencyPairOrEmpty
                    .map(this::isCurrencyPairOrReverseAvailable)
                    .orElse(false);
        } catch (Exception e) {
            log.warn("Exception while check if conversion query is available", e);
            return false;
        }
    }

    @Override
    public ExchangeRate getExchangeRate(ConversionQuery conversionQuery) {
        CurrencyUnit baseCurrencyUnit = conversionQuery.getBaseCurrency();
        CurrencyUnit targetCurrencyUnit = conversionQuery.getCurrency();

        // if any of base or target currency is null we cannot get the rate from xchange exchanges
        if (baseCurrencyUnit == null || targetCurrencyUnit == null) {
            ConversionContext failedConversionContext = createConversionContextBuilder(RateType.ANY).build();
            throw new CurrencyConversionException(baseCurrencyUnit, targetCurrencyUnit, failedConversionContext);
        }

        Optional<ExchangeRate> exchangeRateOrEmpty = getExchangeRateIfAvailable(baseCurrencyUnit, targetCurrencyUnit);
        Optional<ExchangeRate> exchangeRateOrReversedEmpty = exchangeRateOrEmpty.or(() -> {
            // fallback: if the rate is not available, try to get the reverse exchange rate instead
            return getExchangeRateIfAvailable(targetCurrencyUnit, baseCurrencyUnit)
                    .map(this::reverse);
        });

        return exchangeRateOrReversedEmpty.orElseThrow(() -> {
            ConversionContext failedConversionContext = createConversionContextBuilder(RateType.ANY).build();
            return new CurrencyConversionException(baseCurrencyUnit, targetCurrencyUnit, failedConversionContext);
        });
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

    @Override
    public String toString() {
        return this.getContext().toString();
    }

    private boolean isCurrencyPairOrReverseAvailable(CurrencyPair currencyPair) {
        boolean currencyPairAvailable = this.isCurrencyPairAvailable(currencyPair);
        if (currencyPairAvailable) {
            return true;
        }

        CurrencyPair reverseCurrencyPair = MoreCurrencyPairs.reverse(currencyPair);
        return this.isCurrencyPairAvailable(reverseCurrencyPair);
    }

    private boolean isCurrencyPairAvailable(CurrencyPair currencyPair) {
        try {
            return exchange.getExchangeInstruments().contains(currencyPair);
        } catch (Exception e) {
            log.warn("currency pair {} is not available on exchange {}: {}", currencyPair, exchange, e.getMessage());
            return false;
        }
    }

    private Optional<ExchangeRate> getExchangeRateIfAvailable(CurrencyUnit baseCurrencyUnit, CurrencyUnit targetCurrencyUnit) {
        CurrencyPair currencyPair = MoreCurrencyPairs.toCurrencyPair(baseCurrencyUnit, targetCurrencyUnit);

        try {
            if (!isCurrencyPairAvailable(currencyPair)) {
                return Optional.empty();
            }

            Ticker ticker = exchange.getMarketDataService().getTicker(currencyPair);

            ConversionContext conversionContext = createConversionContextFromTicker(ticker);

            Optional<NumberValue> exchangeRateValueOrEmpty = Optional.ofNullable(ticker.getLast())
                    .or(() -> Optional.ofNullable(ticker.getAsk()))
                    .or(() -> Optional.ofNullable(ticker.getBid()))
                    // filter zero as some exchanges return 0 when they do not support the currency pair
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
        } catch (Exception e) {
            String errorMessage = String.format("Exception while fetching exchange rate of %s from exchange %s", currencyPair, exchange);

            log.warn(errorMessage, e);
        }

        return Optional.empty();
    }

    private ConversionContext createConversionContextFromTicker(Ticker ticker) {
        ConversionContextBuilder builder = createConversionContextBuilder(RateType.DEFERRED);
        TickerHelper.createInfoMap(ticker).forEach(builder::set);
        return builder.build();
    }

    private ConversionContextBuilder createConversionContextBuilder(RateType type) {
        return ConversionContextBuilder.create(this.getContext(), type);
    }

    private ExchangeRate reverse(ExchangeRate rate) {
        return new ExchangeRateBuilder(rate)
                .setRate(rate)
                .setBase(rate.getCurrency())
                .setTerm(rate.getBaseCurrency())
                .setFactor(divide(DefaultNumberValue.ONE, rate.getFactor(), MathContext.DECIMAL64))
                .build();
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
