package org.tbk.bitcoin.exchange.example.api.rate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@JsonDeserialize(as = ExchangeRateResponseImpl.ExchangeRateImpl.class)
public interface ExchangeRate {
    String getBase();

    boolean isDerived();

    BigDecimal getFactor();

    String getProvider();

    String getTarget();

    String getType();

    List<ExchangeRate> getChain();

    Map<String, Object> getMeta();
}
