package org.tbk.xchange.jsr354;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

import javax.money.CurrencyUnit;
import javax.money.convert.ConversionQuery;
import java.util.Optional;

final class MoreCurrencyPairs {

    private MoreCurrencyPairs() {
        throw new UnsupportedOperationException();
    }

    static CurrencyPair reverse(CurrencyPair currencyPair) {
        return new CurrencyPair(currencyPair.counter, currencyPair.base);
    }

    static CurrencyPair toCurrencyPair(CurrencyUnit baseCurrencyUnit, CurrencyUnit targetCurrencyUnit) {
        Currency baseCurrency = Currency.getInstance(baseCurrencyUnit.getCurrencyCode());
        Currency targetCurrency = Currency.getInstance(targetCurrencyUnit.getCurrencyCode());

        return new CurrencyPair(baseCurrency, targetCurrency);
    }

    static Optional<CurrencyPair> toCurrencyPair(ConversionQuery conversionQuery) {
        if (conversionQuery.getBaseCurrency() == null || conversionQuery.getCurrency() == null) {
            return Optional.empty();
        }

        CurrencyPair currencyPair = toCurrencyPair(conversionQuery.getBaseCurrency(), conversionQuery.getCurrency());
        return Optional.of(currencyPair);
    }
}
