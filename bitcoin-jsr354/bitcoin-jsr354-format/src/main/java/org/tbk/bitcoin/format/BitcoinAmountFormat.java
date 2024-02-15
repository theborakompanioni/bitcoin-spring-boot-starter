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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BitcoinAmountFormat implements MonetaryAmountFormat {
    private static final String NBSP = "\u00A0"; // No-Break Space
    private static final String NNBSP = "\u202F"; // Narrow No-Break Space
    private static final String SEPARATOR = NBSP;
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
        String[] array = text.toString().split(" " + "|" + NBSP + "|" + NNBSP);

        if (array.length < 2) {
            String errorMessage = String.format("An error happened when try to parse the Monetary Amount. "
                    + "Expected length of greater than or equal to 2 after split, but got: %d", array.length);
            throw new MonetaryParseException(errorMessage, text, 0);
        }

        CurrencyUnit currencyUnit = Monetary.getCurrency(array[0]);

        DecimalFormat df = new DecimalFormat("#,###.#");
        df.setMinimumFractionDigits(currencyUnit.getDefaultFractionDigits());
        df.setMaximumFractionDigits(currencyUnit.getDefaultFractionDigits());
        df.setParseBigDecimal(true);

        String sanitizedNumberValue = Stream.of(array).skip(1)
                .collect(Collectors.joining())
                .replaceAll(NNBSP, "")
                .replaceAll(NBSP, "")
                .replaceAll(" ", "");
        try {
            return context.get(MonetaryAmountFactory.class)
                    .setCurrency(currencyUnit)
                    .setNumber(df.parse(sanitizedNumberValue))
                    .create();
        } catch (ParseException e) {
            throw new MonetaryParseException(e.getMessage(), e.getErrorOffset());
        }
    }

    @Override
    public String queryFrom(MonetaryAmount amount) {
        DecimalFormat df = new DecimalFormat("#,###.#");
        df.setMinimumFractionDigits(amount.getCurrency().getDefaultFractionDigits());
        df.setMaximumFractionDigits(amount.getCurrency().getDefaultFractionDigits());

        BigDecimal number = amount.getNumber().numberValue(BigDecimal.class)
                .setScale(amount.getCurrency().getDefaultFractionDigits(), RoundingMode.HALF_UP);

        String numberFormatted = df.format(number);
        String withSpaces = new StringBuilder(numberFormatted)
                .insert(numberFormatted.indexOf('.') + 3, NNBSP)
                .insert(numberFormatted.indexOf('.') + 7, NNBSP)
                .toString();

        return amount.getCurrency().getCurrencyCode() + SEPARATOR + withSpaces;
    }
}
