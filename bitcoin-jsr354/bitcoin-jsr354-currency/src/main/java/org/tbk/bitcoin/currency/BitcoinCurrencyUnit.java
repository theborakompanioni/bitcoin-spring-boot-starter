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
        return this.getCurrencyCode().compareTo(other.getCurrencyCode());
    }

    @Override
    public int hashCode() {
        return getCurrencyCode().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof CurrencyUnit) {
            CurrencyUnit other = (CurrencyUnit) obj;
            return getCurrencyCode().equals(other.getCurrencyCode());
        }
        return false;
    }

    /**
     * Does the same what {@link java.util.Currency} would do:
     * Returns the unofficial ISO 4217 currency code of bitcoin.
     *
     * @return unofficial ISO 4217 currency code of bitcoin
     */
    @Override
    public String toString() {
        return getCurrencyCode();
    }
}
