package org.tbk.bitcoin.exchange.example;

import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.convert.*;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class BitcoinExchangeRateExampleApplicationTest {

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext, is(notNullValue()));
    }

    @Test
    void dollarsCanBeExchangedForBitcoin() {
        CurrencyConversion btcToUsdConversion = MonetaryConversions.getConversion(ConversionQueryBuilder.of()
                .setBaseCurrency(Monetary.getCurrency("USD"))
                .setTermCurrency(Monetary.getCurrency("BTC"))
                .build());

        Money singleDollar = Money.of(BigDecimal.ONE, "USD");
        Money singleDollarInBtc = singleDollar.with(btcToUsdConversion);

        assertThat("value is in BTC", singleDollarInBtc.getCurrency().getCurrencyCode(), is("BTC"));
        assertThat("value is greater than zero", singleDollarInBtc.isPositive(), is(true));
    }

    @Test
    void bitcoinCanBeExchangeToDollars() {
        CurrencyConversion btcToUsdConversion = MonetaryConversions.getConversion(ConversionQueryBuilder.of()
                .setBaseCurrency(Monetary.getCurrency("BTC"))
                .setTermCurrency(Monetary.getCurrency("USD"))
                .build());

        Money singleBitcoin = Money.of(BigDecimal.ONE, "BTC");
        Money singleBitcoinInUsd = singleBitcoin.with(btcToUsdConversion);

        assertThat("value is in USD", singleBitcoinInUsd.getCurrency().getCurrencyCode(), is("USD"));
        assertThat("value is greater than zero", singleBitcoinInUsd.isPositive(), is(true));
    }

    @Test
    void canFetchAllExchangeRatesForBitcoin() {
        Map<String, ExchangeRateProvider> beansOfType = applicationContext.getBeansOfType(ExchangeRateProvider.class);

        Collection<ExchangeRateProvider> providers = beansOfType.values();
        assertThat(providers, hasSize(greaterThan(0)));

        CurrencyUnit btc = Monetary.getCurrency("BTC");
        CurrencyUnit usd = Monetary.getCurrency("USD");

        ConversionQuery conversionQuery = ConversionQueryBuilder.of()
                .setBaseCurrency(btc)
                .setTermCurrency(usd)
                .build();

        Collection<ExchangeRateProvider> eligibleProvider = providers.stream()
                .filter(it -> it.isAvailable(conversionQuery))
                .toList();
        assertThat(eligibleProvider, hasSize(greaterThan(0)));

        eligibleProvider.forEach(provider -> {
            ExchangeRate exchangeRate = provider.getExchangeRate(conversionQuery);
            assertThat(exchangeRate, is(notNullValue()));

            CurrencyConversion currencyConversion = provider.getCurrencyConversion(conversionQuery);

            Money satoshis = Money.ofMinor(btc, 10_000);
            Money satoshisInUsd = satoshis.with(currencyConversion);

            assertThat(satoshisInUsd.getCurrency(), is(usd));
            assertThat(satoshisInUsd.divide(exchangeRate.getFactor()).getNumberStripped(),
                    is(closeTo(satoshis.getNumberStripped(), BigDecimal.ZERO)));
        });
    }
}
