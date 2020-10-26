package org.tbk.bitcoin.exchange;

import lombok.extern.slf4j.Slf4j;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.ProviderContext;
import javax.money.convert.ProviderContextBuilder;
import javax.money.convert.RateType;

@Slf4j
public class BitcoinStandardExchangeRateProvider extends AbstractStandardExchangeRateProvider implements ExchangeRateProvider {
    private static final ProviderContext providerContext = ProviderContextBuilder.of("BITCOIN-STANDARD", RateType.DEFERRED)
            .set("providerDescription", "Exchange Rate Provider using BTC as base currency.")
            .build();

    private static CurrencyUnit bitcoinCurrencyUnit = Monetary.getCurrency("BTC");

    public BitcoinStandardExchangeRateProvider() {
        super(providerContext);
    }

    @Override
    protected CurrencyUnit getStandardCurrencyUnit() {
        return bitcoinCurrencyUnit;
    }
}
