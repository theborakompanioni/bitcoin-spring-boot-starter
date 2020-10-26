package org.tbk.bitcoin.currency;


import org.javamoney.moneta.Money;
import org.junit.Test;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class BitcoinCurrencyUnitTest {

    @Test
    public void itShouldRegisterBitcoinAsCurrencyCorrectly() {
        CurrencyUnit bitcoinCurrencyUnit = Monetary.getCurrency("BTC");

        assertThat(bitcoinCurrencyUnit).isNotNull();
        assertThat(bitcoinCurrencyUnit.getCurrencyCode()).isEqualTo("BTC");
        assertThat(bitcoinCurrencyUnit.getDefaultFractionDigits()).isEqualTo(8);


        Money singleSatoshi = Money.ofMinor(Monetary.getCurrency("BTC"), 1);
        assertThat(singleSatoshi).isNotNull();

        Money anotherSingleSatoshi = Money.of(new BigDecimal("0.00000001"), "BTC");
        assertThat(anotherSingleSatoshi).isNotNull();
    }

    @Test
    public void itShouldReturnBitcoinForAnyLocale() {
        CurrencyUnit bitcoinCurrencyUnit = Monetary.getCurrency("BTC");

        List<Locale> availableLocales = Arrays.asList(Locale.getAvailableLocales());
        assertThat(availableLocales).hasSizeGreaterThan(0);

        availableLocales.forEach(locale -> {
            Set<CurrencyUnit> currencies = Monetary.getCurrencies(locale);
            assertThat(currencies).contains(bitcoinCurrencyUnit);
        });
    }

    @Test
    public void itShouldBePossibleToMakeMonetaryCalculations() {
        final CurrencyUnit bitcoinCurrencyUnit = Monetary.getCurrency("BTC");
        Money singleSatoshi = Money.ofMinor(bitcoinCurrencyUnit, 1);
        Money anotherSingleSatoshi = Money.of(new BigDecimal("0.00000001"), "BTC");

        Money justMyTwoSatoshis = singleSatoshi.add(anotherSingleSatoshi);

        Money nearlyWholeBitcoin = Money.of(new BigDecimal("0.99999998"), bitcoinCurrencyUnit);

        Money wholeBitcoin = nearlyWholeBitcoin.add(justMyTwoSatoshis);

        assertThat(wholeBitcoin.getNumberStripped()).isEqualByComparingTo(new BigDecimal("1"));
        assertThat(wholeBitcoin.getNumber().numberValue(BigDecimal.class)).isEqualByComparingTo(new BigDecimal("1"));

        Money moreThanABitcoin = wholeBitcoin.add(justMyTwoSatoshis);
        assertThat(moreThanABitcoin.getNumberStripped()).isEqualByComparingTo(new BigDecimal("1.00000002"));
    }
}