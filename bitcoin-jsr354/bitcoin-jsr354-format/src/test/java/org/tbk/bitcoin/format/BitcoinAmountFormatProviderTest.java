package org.tbk.bitcoin.format;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
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
        assertThat(singleSatoshiFormatted, is("BTC 0.00000001"));

        Money singleBitcoin = Money.of(BigDecimal.ONE, bitcoinUnit);
        String singleBitcoinFormatted = sut.format(singleBitcoin);
        assertThat(singleBitcoinFormatted, is("BTC 1"));
    }

    @Test
    void itShouldParseBitcoinAmountCorrectly() {
        MonetaryAmount singleSatoshi = sut.parse("BTC 0.00000001");
        assertThat(singleSatoshi, is(Money.ofMinor(bitcoinUnit, 1)));

        MonetaryAmount singleBitcoin = sut.parse("BTC 1");
        assertThat(singleBitcoin, is(Money.of(BigDecimal.ONE, bitcoinUnit)));
    }

}