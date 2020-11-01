package org.tbk.bitcoin.exchange.example;

import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.tbk.xchange.jsr354.XChangeExchangeRateProvider;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.convert.ConversionQuery;
import javax.money.convert.ConversionQueryBuilder;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.ExchangeRate;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BitcoinExchangeApplicationTest {

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Test
    public void contextLoads() {
        assertThat(applicationContext, is(notNullValue()));
    }

    @Test
    public void canFetchExchangeRatesForBitcoin() {
        final Map<String, XChangeExchangeRateProvider> beansOfType = applicationContext.getBeansOfType(XChangeExchangeRateProvider.class);

        Collection<XChangeExchangeRateProvider> providers = beansOfType.values();
        assertThat(providers, hasSize(greaterThan(0)));

        CurrencyUnit btc = Monetary.getCurrency("BTC");
        CurrencyUnit usd = Monetary.getCurrency("USD");

        ConversionQuery conversionQuery = ConversionQueryBuilder.of()
                .setBaseCurrency(btc)
                .setTermCurrency(usd)
                .build();

        providers.forEach(provider -> {
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
