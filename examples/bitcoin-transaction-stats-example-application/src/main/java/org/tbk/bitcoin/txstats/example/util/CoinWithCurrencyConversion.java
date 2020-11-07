package org.tbk.bitcoin.txstats.example.util;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.bitcoinj.core.Coin;
import org.javamoney.moneta.Money;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.convert.CurrencyConversion;

@Value
@Builder
public class CoinWithCurrencyConversion {
    private static final CurrencyUnit BTC = Monetary.getCurrency("BTC");

    @NonNull
    Coin coin;

    @NonNull
    CurrencyConversion currencyConversion;

    public String toFriendlyString() {
        Money btc = Money.ofMinor(BTC, coin.getValue());
        Money other = btc.with(currencyConversion);

        return new StringBuilder()
                .append(coin.toFriendlyString())
                .append(" ")
                .append("(")
                .append(other.toString())
                .append(")")
                .toString();
    }
}
