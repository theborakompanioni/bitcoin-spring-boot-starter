package org.tbk.bitcoin.format;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.MonetaryAmountFactory;
import javax.money.format.AmountFormatContext;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryParseException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class BitcoinAmountFormat implements MonetaryAmountFormat {
    private static final String SEPARATOR = " ";
    private final AmountFormatContext context;

    BitcoinAmountFormat(AmountFormatContext context) {
        this.context = context;
    }

    @Override
    public AmountFormatContext getContext() {
        return context;
    }

    @Override
    public void print(Appendable appendable, MonetaryAmount amount) throws IOException {
        appendable.append(queryFrom(amount));
    }

    @Override
    public MonetaryAmount parse(CharSequence text) throws MonetaryParseException {
        String[] array = text.toString().split(SEPARATOR);
        if (array.length != 2) {
            String errorMessage = String.format("An error happened when try to parse the Monetary Amount. "
                    + "Expected length of 2 after split, but got: %d", array.length);
            throw new MonetaryParseException(errorMessage, text, 0);
        }

        CurrencyUnit currencyUnit = Monetary.getCurrency(array[0]);
        BigDecimal number = new BigDecimal(array[1]);

        return context.get(MonetaryAmountFactory.class)
                .setCurrency(currencyUnit)
                .setNumber(number)
                .create();
    }

    @Override
    public String queryFrom(MonetaryAmount amount) {
        BigDecimal number = amount.getNumber().numberValue(BigDecimal.class)
                .setScale(amount.getCurrency().getDefaultFractionDigits(), RoundingMode.HALF_UP);

        String numberFormatted = number
                .stripTrailingZeros()
                .toPlainString();

        return amount.getCurrency().getCurrencyCode() + SEPARATOR + numberFormatted;
    }
}
