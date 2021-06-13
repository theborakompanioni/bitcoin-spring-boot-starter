package org.tbk.bitcoin.example.payreq.common.db.jsr354;

import org.javamoney.moneta.spi.DefaultNumberValue;

import javax.money.NumberValue;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.math.BigDecimal;

/**
 * JPA {@link AttributeConverter} to serialize {@link javax.money.NumberValue} instances into a {@link String}. Auto-applied to
 * all entity properties of type {@link NumberValue}.
 */
@Converter(autoApply = true)
public class NumberValueAttributeConverter implements AttributeConverter<NumberValue, String> {

    @Override
    public String convertToDatabaseColumn(NumberValue numberValue) {
        return numberValue == null ? null : numberValue.numberValue(BigDecimal.class).toPlainString();
    }

    @Override
    public NumberValue convertToEntityAttribute(String source) {
        if (source == null) {
            return null;
        }

        return DefaultNumberValue.of(new BigDecimal(source));
    }
}