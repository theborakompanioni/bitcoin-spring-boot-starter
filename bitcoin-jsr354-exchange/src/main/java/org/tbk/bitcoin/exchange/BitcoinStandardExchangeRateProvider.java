package org.tbk.bitcoin.exchange;

import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.convert.ExchangeRateBuilder;
import org.javamoney.moneta.spi.AbstractRateProvider;
import org.javamoney.moneta.spi.LazyBoundCurrencyConversion;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.convert.*;
import java.util.stream.Stream;

@Slf4j
public class BitcoinStandardExchangeRateProvider extends AbstractRateProvider implements ExchangeRateProvider {
    private static final ProviderContext providerContext = ProviderContextBuilder.of("BITCOIN-STANDARD", RateType.DEFERRED)
            .set("providerDescription", "BitcoinStandardExchangeRateProvider")
            .build();

    public BitcoinStandardExchangeRateProvider() {
        super(providerContext);
    }

    @Override
    public ExchangeRate getExchangeRate(ConversionQuery conversionQuery) {
        CurrencyUnit bitcoinCurrencyUnit = Monetary.getCurrency("BTC");

        CurrencyUnit baseCurrencyUnit = conversionQuery.getBaseCurrency();
        CurrencyUnit targetCurrencyUnit = conversionQuery.getCurrency();

        boolean isDirectBtcQuery = bitcoinCurrencyUnit.equals(baseCurrencyUnit) ||
                bitcoinCurrencyUnit.equals(targetCurrencyUnit);

        if (isDirectBtcQuery) {
            // do not handle XXX -> BTC or BTC -> XXX conversions!
            // a downstream provider should take care of these
            return null;
        }

        ConversionQuery btcToBase = conversionQuery.toBuilder()
                .setBaseCurrency(bitcoinCurrencyUnit)
                .setTermCurrency(baseCurrencyUnit)
                .build();

        ExchangeRateProvider btcToBaseExchangeRateProvider = MonetaryConversions.getExchangeRateProvider(btcToBase);

        ConversionQuery btcToTarget = conversionQuery.toBuilder()
                .setBaseCurrency(bitcoinCurrencyUnit)
                .setTermCurrency(targetCurrencyUnit)
                .build();

        ExchangeRateProvider btcToTargetExchangeRateProvider = MonetaryConversions.getExchangeRateProvider(btcToTarget);


        boolean baseOrTargetPairNotFound = btcToTargetExchangeRateProvider == null ||
                btcToBaseExchangeRateProvider == null;

        if (baseOrTargetPairNotFound) {
            return null;
        }

        ExchangeRate btcToBaseExchangeRate = btcToBaseExchangeRateProvider.getExchangeRate(btcToBase);
        ExchangeRate btcToTargetExchangeRate = btcToTargetExchangeRateProvider.getExchangeRate(btcToTarget);

        RateType btcToBaseRateType = btcToBaseExchangeRate.getContext().getRateType();
        RateType btcToTargetRateType = btcToBaseExchangeRate.getContext().getRateType();

        RateType combinedRateType = Stream.of(btcToBaseRateType, btcToTargetRateType)
                .reduce(this::combineRateType)
                .orElse(RateType.OTHER);

        ConversionContext conversionContext = ConversionContext.of(this.getContext().getProviderName(), combinedRateType);
        ExchangeRateBuilder builder = new ExchangeRateBuilder(conversionContext);
        builder.setBase(baseCurrencyUnit);
        builder.setTerm(targetCurrencyUnit);
        builder.setFactor(divide(btcToTargetExchangeRate.getFactor(), btcToBaseExchangeRate.getFactor()));
        builder.setRateChain(btcToTargetExchangeRate, btcToBaseExchangeRate);

        return builder.build();
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
