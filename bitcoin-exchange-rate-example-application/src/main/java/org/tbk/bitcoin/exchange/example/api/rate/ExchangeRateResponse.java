package org.tbk.bitcoin.exchange.example.api.rate;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.money.CurrencyUnit;
import java.time.LocalDate;
import java.util.List;

@JsonDeserialize(as = ExchangeRateResponseImpl.class)
public interface ExchangeRateResponse {
    String getBase();

    List<? extends ExchangeRate> getRates();
}
