package org.tbk.bitcoin.example.payreq.common.db.jsr354;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

/**
 * JPA {@link AttributeConverter} to serialize {@link CurrencyUnit} instances into a {@link String}. Auto-applied to
 * all entity properties of type {@link CurrencyUnit}.
 */
@Converter(autoApply = true)
public class CurrencyUnitAttributeConverter implements AttributeConverter<CurrencyUnit, String> {

    @Override
    public String convertToDatabaseColumn(CurrencyUnit currencyUnit) {
        return currencyUnit == null ? null : currencyUnit.getCurrencyCode();
    }

    @Override
    public CurrencyUnit convertToEntityAttribute(String source) {
        if (source == null) {
            return null;
        }

        return Monetary.getCurrency(source);
    }
}