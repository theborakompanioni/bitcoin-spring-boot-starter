package org.tbk.bitcoin.example.payreq.common;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

public interface Currencies {
    CurrencyUnit BTC = Monetary.getCurrency("BTC");
    CurrencyUnit USD = Monetary.getCurrency("USD");
    CurrencyUnit EUR = Monetary.getCurrency("EUR");
}
