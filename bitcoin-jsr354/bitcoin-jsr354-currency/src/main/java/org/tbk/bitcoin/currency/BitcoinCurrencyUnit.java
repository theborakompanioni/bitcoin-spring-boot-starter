package org.tbk.bitcoin.currency;

import javax.money.CurrencyContext;
import javax.money.CurrencyUnit;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

public final class BitcoinCurrencyUnit implements CurrencyUnit, Serializable, Comparable<CurrencyUnit> {
    private static final String CURRENCY_CODE = "BTC";
    private static final int FRACTION_DIGITS = 8;
    private static final int NUMERIC_CODE = -1;

    private final CurrencyContext context;

    BitcoinCurrencyUnit(CurrencyContext context) {
        this.context = requireNonNull(context);
    }

    @Override
    public String getCurrencyCode() {
        return CURRENCY_CODE;
    }

    @Override
    public int getNumericCode() {
        return NUMERIC_CODE;
    }

    @Override
    public int getDefaultFractionDigits() {
        return FRACTION_DIGITS;
    }

    @Override
    public CurrencyContext getContext() {
        return context;
    }

    @Override
    public int compareTo(CurrencyUnit other) {
        requireNonNull(other);

        return this.getCurrencyCode().compareTo(other.getCurrencyCode());
    }
}
