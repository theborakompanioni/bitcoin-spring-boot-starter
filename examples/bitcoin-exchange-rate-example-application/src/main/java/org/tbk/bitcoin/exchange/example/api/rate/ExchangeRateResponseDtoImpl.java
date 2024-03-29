package org.tbk.bitcoin.exchange.example.api.rate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.money.AbstractContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Value
@Builder
@Jacksonized
public class ExchangeRateResponseDtoImpl implements ExchangeRateResponseDto {

    String base;
    List<? extends ExchangeRateDto> rates;

    @Value
    @Builder
    @Jacksonized
    public static class ExchangeRateDtoImpl implements ExchangeRateDto {

        private static final class ContextUtil {
            public static Map<String, Object> toMap(AbstractContext context) {
                return context.getKeys(Object.class).stream()
                        .collect(Collectors.toMap(val -> val, val -> context.get(val, Object.class)));
            }
        }

        public static ExchangeRateDtoImplBuilder toDto(javax.money.convert.ExchangeRate exchangeRate) {
            return ExchangeRateDtoImpl.builder()
                    .factor(new BigDecimal(exchangeRate.getFactor().toString()))
                    .base(exchangeRate.getBaseCurrency().getCurrencyCode())
                    .target(exchangeRate.getCurrency().getCurrencyCode())
                    .provider(exchangeRate.getContext().getProviderName())
                    .derived(exchangeRate.isDerived())
                    .type(exchangeRate.getContext().getRateType().name())
                    .chain(exchangeRate.getExchangeRateChain().stream()
                            .filter(val -> !exchangeRate.equals(val))
                            .map(ExchangeRateDtoImpl::toDto)
                            .map(ExchangeRateDtoImplBuilder::build)
                            .toList())
                    .meta(ContextUtil.toMap(exchangeRate.getContext()))
                    .date(exchangeRate.getContext().get(LocalDate.class));
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
        List<ExchangeRateDto> chain;

        @Singular("addMeta")
        Map<String, Object> meta;
    }
}
