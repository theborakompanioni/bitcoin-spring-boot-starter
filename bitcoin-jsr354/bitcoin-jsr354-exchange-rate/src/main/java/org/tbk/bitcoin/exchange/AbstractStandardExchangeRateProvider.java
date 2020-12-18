package org.tbk.bitcoin.exchange;

import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.convert.ExchangeRateBuilder;
import org.javamoney.moneta.spi.AbstractRateProvider;

import javax.money.CurrencyUnit;
import javax.money.convert.*;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
abstract class AbstractStandardExchangeRateProvider extends AbstractRateProvider implements ExchangeRateProvider {

    protected AbstractStandardExchangeRateProvider(ProviderContext providerContext) {
        super(providerContext);
    }

    /**
     * @return the standard currency the provider should use as base currency.
     */
    protected abstract CurrencyUnit getStandardCurrencyUnit();

    @Override
    public boolean isAvailable(ConversionQuery conversionQuery) {
        CurrencyUnit standardCurrencyUnit = getStandardCurrencyUnit();

        CurrencyUnit baseCurrencyUnit = conversionQuery.getBaseCurrency();
        CurrencyUnit targetCurrencyUnit = conversionQuery.getCurrency();

        boolean isDirectStdQuery = standardCurrencyUnit.equals(baseCurrencyUnit) ||
                standardCurrencyUnit.equals(targetCurrencyUnit);

        if (isDirectStdQuery) {
            // do not handle XXX -> STD or STD -> XXX conversions!
            // a downstream provider should take care of these
            return false;
        }

        ConversionQuery stdToBase = conversionQuery.toBuilder()
                .setBaseCurrency(standardCurrencyUnit)
                .setTermCurrency(baseCurrencyUnit)
                .build();

        boolean stdToBaseExchangeRateAvailable = Optional.ofNullable(MonetaryConversions.getExchangeRateProvider(stdToBase))
                .map(it -> it.isAvailable(stdToBase))
                .orElse(false);

        if (!stdToBaseExchangeRateAvailable) {
            // leave method early if one rate is not available
            // we need both rates to support the conversion
            return false;
        }

        ConversionQuery stdToTarget = conversionQuery.toBuilder()
                .setBaseCurrency(standardCurrencyUnit)
                .setTermCurrency(targetCurrencyUnit)
                .build();

        boolean stdToTargetExchangeRateAvailable = Optional.ofNullable(MonetaryConversions.getExchangeRateProvider(stdToTarget))
                .map(it -> it.isAvailable(stdToTarget))
                .orElse(false);

        return stdToTargetExchangeRateAvailable;
    }

    @Override
    public ExchangeRate getExchangeRate(ConversionQuery conversionQuery) {
        if (!isAvailable(conversionQuery)) {
            return null;
        }

        CurrencyUnit standardCurrencyUnit = getStandardCurrencyUnit();

        CurrencyUnit baseCurrencyUnit = conversionQuery.getBaseCurrency();
        CurrencyUnit targetCurrencyUnit = conversionQuery.getCurrency();

        ConversionQuery stdToBase = conversionQuery.toBuilder()
                .setBaseCurrency(standardCurrencyUnit)
                .setTermCurrency(baseCurrencyUnit)
                .build();

        ExchangeRate stdToBaseExchangeRate = Optional.ofNullable(MonetaryConversions.getExchangeRateProvider(stdToBase))
                .filter(val -> val.isAvailable(stdToBase))
                .map(val -> val.getExchangeRate(stdToBase))
                .orElse(null);

        if (stdToBaseExchangeRate == null) {
            return null;
        }

        ConversionQuery stdToTarget = conversionQuery.toBuilder()
                .setBaseCurrency(standardCurrencyUnit)
                .setTermCurrency(targetCurrencyUnit)
                .build();

        ExchangeRate stdToTargetExchangeRate = Optional.ofNullable(MonetaryConversions.getExchangeRateProvider(stdToTarget))
                .filter(val -> val.isAvailable(stdToTarget))
                .map(val -> val.getExchangeRate(stdToTarget))
                .orElse(null);

        if (stdToTargetExchangeRate == null) {
            return null;
        }

        RateType stdToBaseRateType = stdToBaseExchangeRate.getContext().getRateType();
        RateType stdToTargetRateType = stdToBaseExchangeRate.getContext().getRateType();

        RateType combinedRateType = Stream.of(stdToBaseRateType, stdToTargetRateType)
                .reduce(this::combineRateType)
                .orElse(RateType.OTHER);

        ConversionContext conversionContext = ConversionContext.from(this.getContext(), combinedRateType);

        return new ExchangeRateBuilder(conversionContext)
                .setBase(baseCurrencyUnit)
                .setTerm(targetCurrencyUnit)
                .setFactor(divide(stdToTargetExchangeRate.getFactor(), stdToBaseExchangeRate.getFactor()))
                .setRateChain(stdToBaseExchangeRate, stdToTargetExchangeRate)
                .build();
    }

    private RateType combineRateType(RateType r1, RateType r2) {
        if (r1.equals(r2)) {
            return r1;
        }

        // handle REALTIME
        if (RateType.REALTIME.equals(r1)) {
            return r2;
        }
        if (RateType.REALTIME.equals(r2)) {
            return r1;
        }

        // handle DEFERRED
        if (RateType.DEFERRED.equals(r1)) {
            return r2;
        }
        if (RateType.DEFERRED.equals(r2)) {
            return r1;
        }

        // handle HISTORIC
        if (RateType.HISTORIC.equals(r1)) {
            return r2;
        }
        if (RateType.HISTORIC.equals(r2)) {
            return r1;
        }

        return RateType.OTHER;
    }
}
