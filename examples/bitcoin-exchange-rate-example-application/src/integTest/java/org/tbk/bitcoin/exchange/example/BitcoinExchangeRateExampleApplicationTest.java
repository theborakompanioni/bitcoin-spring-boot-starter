package org.tbk.bitcoin.exchange.example;

import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.convert.*;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class BitcoinExchangeRateExampleApplicationTest {

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Test
    public void contextLoads() {
        assertThat(applicationContext, is(notNullValue()));
    }

    @Test
    public void dollarsCanBeExchangedForBitcoin() {
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
    public void bitcoinCanBeExchangeToDollars() {
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
    public void canFetchAllExchangeRatesForBitcoin() {
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
                .collect(Collectors.toList());
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
