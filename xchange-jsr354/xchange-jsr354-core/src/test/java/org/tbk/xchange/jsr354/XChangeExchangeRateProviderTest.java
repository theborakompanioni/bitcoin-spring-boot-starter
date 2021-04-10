package org.tbk.xchange.jsr354;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;

import javax.money.convert.*;
import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class XChangeExchangeRateProviderTest {

    private XChangeExchangeRateProvider sut;

    @BeforeEach
    public void setUp() {
        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(DummyExchange.class);
        ProviderContext providerContext = MoreProviderContexts.createSimpleProviderContextBuilder(exchange).build();
        this.sut = new XChangeExchangeRateProvider(providerContext, exchange);
    }

    @Test
    public void itShouldFetchSupportedExchangeRateSuccessfully() {
        ExchangeRate exchangeRate = this.sut.getExchangeRate("BTC", "USD");

        assertThat(exchangeRate, is(notNullValue()));

        BigDecimal singleBitcoinInUsdValue = exchangeRate.getFactor().numberValue(BigDecimal.class);
        assertThat("bitcoin value in usd is zero or greater", singleBitcoinInUsdValue, is(greaterThanOrEqualTo(BigDecimal.ZERO)));
    }

    @Test
    public void itShouldNotFetchUnsupportedExchangeRate() {
        try {
            ExchangeRate ignoredOnPurpose = this.sut.getExchangeRate("BTC", "BTC");

            Assertions.fail("Should have thrown exception");
        } catch (CurrencyConversionException e) {
            assertThat(e, is(notNullValue()));
        }
    }

    @Test
    public void itShouldFetchSupportedCurrencyConversion() {
        ConversionQuery conversionQuery = ConversionQueryBuilder.of()
                .setBaseCurrency("BTC")
                .setTermCurrency("USD")
                .build();

        CurrencyConversion currencyConversion = this.sut.getCurrencyConversion(conversionQuery);
        assertThat(currencyConversion, is(notNullValue()));

        ExchangeRateProvider exchangeRateProvider = currencyConversion.getExchangeRateProvider();
        assertThat(exchangeRateProvider.isAvailable("BTC", "USD"), is(true));
        assertThat(exchangeRateProvider.isAvailable(conversionQuery), is(true));

        ProviderContext context = exchangeRateProvider.getContext();
        assertThat(context, is(this.sut.getContext()));

        ExchangeRate exchangeRate = exchangeRateProvider.getExchangeRate(conversionQuery);

        BigDecimal singleBitcoinInUsdValue = exchangeRate.getFactor().numberValue(BigDecimal.class);
        assertThat("bitcoin value in usd is zero or greater", singleBitcoinInUsdValue, is(greaterThanOrEqualTo(BigDecimal.ZERO)));
    }

    @Test
    public void itShouldNotFetchUnsupportedCurrencyConversion() {
        ConversionQuery conversionQuery = ConversionQueryBuilder.of()
                .setBaseCurrency("BTC")
                .setTermCurrency("BTC")
                .build();

        CurrencyConversion currencyConversion = this.sut.getCurrencyConversion(conversionQuery);
        assertThat(currencyConversion, is(notNullValue()));

        ExchangeRateProvider exchangeRateProvider = currencyConversion.getExchangeRateProvider();
        assertThat(exchangeRateProvider.isAvailable("BTC", "BTC"), is(false));
        assertThat(exchangeRateProvider.isAvailable(conversionQuery), is(false));

        try {
            ExchangeRate exchangeRate = currencyConversion.getExchangeRate(Money.of(BigDecimal.ONE, "BTC"));

            Assertions.fail("Should have thrown exception");
        } catch (CurrencyConversionException e) {
            assertThat(e, is(notNullValue()));
        }
    }
}