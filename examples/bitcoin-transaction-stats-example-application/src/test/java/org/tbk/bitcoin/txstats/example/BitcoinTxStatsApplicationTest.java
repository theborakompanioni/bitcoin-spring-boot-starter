package org.tbk.bitcoin.txstats.example;

import lombok.extern.slf4j.Slf4j;
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
import javax.money.convert.*;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class BitcoinTxStatsApplicationTest {

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
}
