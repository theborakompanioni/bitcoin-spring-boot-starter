package org.tbk.bitcoin.exchange.example.api.rate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import javax.money.AbstractContext;
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

    String base;
    List<? extends ExchangeRate> rates;

    @Value
    @Builder
    @JsonDeserialize(builder = ExchangeRateImpl.ExchangeRateImplBuilder.class)
    public static class ExchangeRateImpl implements ExchangeRate {

        private static final class ContextUtil {
            public static Map<String, Object> toMap(AbstractContext context) {
                return context.getKeys(Object.class).stream()
                        .collect(Collectors.toMap(val -> val, val -> context.get(val, Object.class)));
            }
        }

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
                    .meta(ContextUtil.toMap(exchangeRate.getContext()))
                    .date(exchangeRate.getContext().get(LocalDate.class));
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static final class ExchangeRateImplBuilder {
        }

        String base;
        boolean derived;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal factor;

        String provider;
        String target;
        String type;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        LocalDate date;

        @Singular("addChain")
        List<ExchangeRate> chain;

        @Singular("addMeta")
        Map<String, Object> meta;
    }
}
