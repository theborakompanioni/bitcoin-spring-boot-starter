package org.tbk.bitcoin.example.payreq.common.db.bitcoinj;

import org.bitcoinj.core.Sha256Hash;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * JPA {@link AttributeConverter} to serialize {@link Sha256Hash} instances into a {@link String}. Auto-applied to
 * all entity properties of type {@link Sha256Hash}.
 */
@Converter(autoApply = true)
public class Sha256HashAttributeConverter implements AttributeConverter<Sha256Hash, String> {

    @Override
    public String convertToDatabaseColumn(Sha256Hash hash) {
        return hash == null ? null : hash.toString();
    }

    @Override
    public Sha256Hash convertToEntityAttribute(String source) {
        if (source == null) {
            return null;
        }

        return Sha256Hash.wrap(source);
    }
}