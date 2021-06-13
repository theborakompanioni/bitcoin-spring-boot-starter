package org.tbk.bitcoin.currency.format;

import org.tbk.bitcoin.currency.BitcoinCurrencyProvider;

import javax.money.format.AmountFormatContextBuilder;
import javax.money.format.AmountFormatQuery;
import javax.money.format.MonetaryAmountFormat;
import javax.money.spi.MonetaryAmountFormatProviderSpi;
import java.util.*;

import static java.util.Objects.requireNonNull;

public final class BitcoinAmountFormatProvider implements MonetaryAmountFormatProviderSpi {
    private static final String FORMAT_NAME_BITCOIN = "bitcoin";
    private static final Set<String> FORMAT_NAMES = Set.of(FORMAT_NAME_BITCOIN);

    public static String formatNameBitcoin() {
        return FORMAT_NAME_BITCOIN;
    }

    @Override
    public String getProviderName() {
        return BitcoinCurrencyProvider.providerName();
    }

    @Override
    public Set<Locale> getAvailableLocales() {
        return Set.of(Locale.getAvailableLocales());
    }

    @Override
    public Set<String> getAvailableFormatNames() {
        return FORMAT_NAMES;
    }

    @Override
    public Collection<MonetaryAmountFormat> getAmountFormats(AmountFormatQuery amountFormatQuery) {
        requireNonNull(amountFormatQuery, "AmountFormatContext required");

        if (!amountFormatQuery.getProviderNames().isEmpty() &&
                !amountFormatQuery.getProviderNames().contains(getProviderName())) {
            return Collections.emptySet();
        }
        if (!(amountFormatQuery.getFormatName() == null
                || getAvailableFormatNames().contains(amountFormatQuery.getFormatName().toLowerCase()))) {
            return Collections.emptySet();
        }
        AmountFormatContextBuilder builder = AmountFormatContextBuilder.of(getProviderName());
        if (amountFormatQuery.getLocale() != null) {
            builder.setLocale(amountFormatQuery.getLocale());
        }
        builder.importContext(amountFormatQuery, false);
        builder.setMonetaryAmountFactory(amountFormatQuery.getMonetaryAmountFactory());
        return Arrays.asList(new MonetaryAmountFormat[]{new BitcoinAmountFormat(builder.build())});
    }
}
