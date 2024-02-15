package org.tbk.bitcoin.format;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class BitcoinAmountFormatProviderTest {
    private static final CurrencyUnit bitcoinUnit = Monetary.getCurrency("BTC");
    private static final MonetaryAmountFormat sut = MonetaryFormats
            .getAmountFormat(BitcoinAmountFormatProvider.formatNameBitcoin());

    @Test
    void itShouldRegisterBitcoinAsMonetaryAmountFormatCorrectly() {
        MonetaryAmountFormat bitcoinAmountFormat = MonetaryFormats
                .getAmountFormat(BitcoinAmountFormatProvider.formatNameBitcoin());

        assertThat(bitcoinAmountFormat, is(notNullValue()));

        String formatName = bitcoinAmountFormat.getContext().getFormatName();
        assertThat(formatName, is("BitcoinAmountFormatProvider"));
    }

    @Test
    void itShouldFormatBitcoinAmountCorrectly() {
        Money singleSatoshi = Money.ofMinor(bitcoinUnit, 1);
        String singleSatoshiFormatted = sut.format(singleSatoshi);
        assertThat(singleSatoshiFormatted, is("BTC 0.00\u202F000\u202F001"));

        Money singleBitcoin = Money.of(BigDecimal.ONE, bitcoinUnit);
        String singleBitcoinFormatted = sut.format(singleBitcoin);
        assertThat(singleBitcoinFormatted, is("BTC 1.00\u202F000\u202F000"));

        Money money1 = Money.ofMinor(bitcoinUnit, 1).multiply(21_000);
        String money1Formatted = sut.format(money1);
        assertThat(money1Formatted, is("BTC 0.00\u202F021\u202F000"));

        Money money2 = Money.of(BigDecimal.ONE, bitcoinUnit).multiply(21_000_000);
        String money2Formatted = sut.format(money2);
        assertThat(money2Formatted, is("BTC 21,000,000.00\u202F000\u202F000"));
    }

    @Test
    void itShouldParseBitcoinAmountCorrectly() {
        assertThat(sut.parse("BTC 0.00 000 001"), is(Money.ofMinor(bitcoinUnit, 1)));
        assertThat(sut.parse("BTC 0.00 000\u202F001"), is(Money.ofMinor(bitcoinUnit, 1)));
        assertThat(sut.parse("BTC 0.00\u202F000\u202F001"), is(Money.ofMinor(bitcoinUnit, 1)));

        assertThat(sut.parse("BTC 1"), is(Money.of(BigDecimal.ONE, bitcoinUnit)));
        assertThat(sut.parse("BTC 1."), is(Money.of(BigDecimal.ONE, bitcoinUnit)));
        assertThat(sut.parse("BTC 1.0"), is(Money.of(BigDecimal.ONE, bitcoinUnit)));
        assertThat(sut.parse("BTC 1.000"), is(Money.of(BigDecimal.ONE, bitcoinUnit)));
        assertThat(sut.parse("BTC 1.00 000 000"), is(Money.of(BigDecimal.ONE, bitcoinUnit)));
        assertThat(sut.parse("BTC 1.00 000\u202F000"), is(Money.of(BigDecimal.ONE, bitcoinUnit)));
        assertThat(sut.parse("BTC 1"), is(Money.of(BigDecimal.ONE, bitcoinUnit)));

        Money money1 = Money.ofMinor(bitcoinUnit, 1).multiply(21_000);
        assertThat(sut.parse(sut.format(money1)), is(money1));

        Money money2 = Money.of(BigDecimal.ONE, bitcoinUnit).multiply(21_000_000);
        assertThat(sut.parse(sut.format(money2)), is(money2));
    }

}