package org.tbk.bitcoin.exchange.example.api.rate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder
@JsonDeserialize(builder = ExchangeRateResponseImpl.ExchangeRateResponseImplBuilder.class)
public class ExchangeRateResponseImpl implements ExchangeRateResponse {
    @JsonPOJOBuilder(withPrefix = "")
    public static final class ExchangeRateResponseImplBuilder {
    }

    private String base;
    private LocalDate date;
    private List<? extends ExchangeRate> rates;

    @Value
    @Builder
    @JsonDeserialize(builder = ExchangeRateImpl.ExchangeRateImplBuilder.class)
    public static class ExchangeRateImpl implements ExchangeRate {
        @JsonPOJOBuilder(withPrefix = "")
        public static final class ExchangeRateImplBuilder {
        }

        private String base;
        private boolean derived;
        private double factor;
        private String provider;
        private String target;
        private String type;
    }
}
