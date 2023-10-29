package org.tbk.bitcoin.exchange.example.api.rate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@JsonDeserialize(as = ExchangeRateResponseDtoImpl.ExchangeRateDtoImpl.class)
public interface ExchangeRateDto {
    String getBase();

    boolean isDerived();

    BigDecimal getFactor();

    String getProvider();

    String getTarget();

    String getType();

    List<ExchangeRateDto> getChain();

    Map<String, Object> getMeta();
}
