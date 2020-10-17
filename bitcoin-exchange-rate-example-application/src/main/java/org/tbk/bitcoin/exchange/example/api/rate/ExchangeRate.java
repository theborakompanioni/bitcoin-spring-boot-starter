package org.tbk.bitcoin.exchange.example.api.rate;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ExchangeRateResponseImpl.ExchangeRateImpl.class)
public interface ExchangeRate {
    String getBase();

    boolean isDerived();

    double getFactor();

    String getProvider();

    String getTarget();

    String getType();
}
