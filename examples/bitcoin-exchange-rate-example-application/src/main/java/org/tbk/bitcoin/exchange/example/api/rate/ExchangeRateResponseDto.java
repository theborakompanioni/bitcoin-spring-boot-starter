package org.tbk.bitcoin.exchange.example.api.rate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

@JsonDeserialize(as = ExchangeRateResponseDtoImpl.class)
public interface ExchangeRateResponseDto {
    String getBase();

    List<? extends ExchangeRateDto> getRates();
}
