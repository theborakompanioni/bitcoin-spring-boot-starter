package org.tbk.bitcoin.example.payreq.common;

import org.tbk.bitcoin.format.BitcoinAmountFormatProvider;

import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;

public interface MonetaryAmountFormats {
    MonetaryAmountFormat bitcoin = MonetaryFormats
            .getAmountFormat(BitcoinAmountFormatProvider.formatNameBitcoin());
}
