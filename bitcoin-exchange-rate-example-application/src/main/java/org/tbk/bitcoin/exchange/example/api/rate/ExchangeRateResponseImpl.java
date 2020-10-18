package org.tbk.bitcoin.exchange.example.api.rate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Value
@Builder
@JsonDeserialize(builder = ExchangeRateResponseImpl.ExchangeRateResponseImplBuilder.class)
public class ExchangeRateResponseImpl implements ExchangeRateResponse {
    @JsonPOJOBuilder(withPrefix = "")
    public static final class ExchangeRateResponseImplBuilder {
    }

    private String base;
    private List<? extends ExchangeRate> rates;

    @Value
    @Builder
    @JsonDeserialize(builder = ExchangeRateImpl.ExchangeRateImplBuilder.class)
    public static class ExchangeRateImpl implements ExchangeRate {

        public static ExchangeRateImplBuilder toDto(javax.money.convert.ExchangeRate exchangeRate) {
            return ExchangeRateResponseImpl.ExchangeRateImpl.builder()
                    .factor(new BigDecimal(exchangeRate.getFactor().toString()))
                    .base(exchangeRate.getBaseCurrency().getCurrencyCode())
                    .target(exchangeRate.getCurrency().getCurrencyCode())
                    .provider(exchangeRate.getContext().getProviderName())
                    .derived(exchangeRate.isDerived())
                    .type(exchangeRate.getContext().getRateType().name())
                    .chain(exchangeRate.getExchangeRateChain().stream()
                            .filter(val -> !exchangeRate.equals(val))
                            .map(ExchangeRateImpl::toDto)
                            .map(ExchangeRateImplBuilder::build)
                            .collect(Collectors.toList()))
                    .date(exchangeRate.getContext().get(LocalDate.class));
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static final class ExchangeRateImplBuilder {
        }

        private String base;
        private boolean derived;

        private BigDecimal factor;
        private String provider;
        private String target;
        private String type;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private LocalDate date;

        @Singular("addChain")
        private List<ExchangeRate> chain;

        @Singular("addMeta")
        private Map<String, Object> meta;
    }
}
