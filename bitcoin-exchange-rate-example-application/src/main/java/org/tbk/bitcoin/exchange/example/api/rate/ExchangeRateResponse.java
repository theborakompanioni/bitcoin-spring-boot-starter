package org.tbk.bitcoin.exchange.example.api.rate;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDate;
import java.util.List;

@JsonDeserialize(as = ExchangeRateResponseImpl.class)
public interface ExchangeRateResponse {
    String getBase();

    LocalDate getDate();

    List<? extends ExchangeRate> getRates();
}
