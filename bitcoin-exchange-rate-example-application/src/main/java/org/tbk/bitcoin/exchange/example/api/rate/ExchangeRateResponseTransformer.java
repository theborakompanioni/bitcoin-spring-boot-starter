package org.tbk.bitcoin.exchange.example.api.rate;

import lombok.Builder;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import javax.money.CurrencyUnit;
import javax.money.convert.ConversionQueryBuilder;
import javax.money.convert.ExchangeRateProvider;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

class ExchangeRateResponseTransformer implements Function<Flux<CurrencyUnit>, Publisher<ExchangeRateResponse>> {
    static ExchangeRateResponseTransformer ofTodayWithSlidingWindow(ExchangeRateProvider provider, CurrencyUnit baseCurrency) {
        return withSlidingWindow(provider, baseCurrency, LocalDate.now());
    }

    static ExchangeRateResponseTransformer withSlidingWindow(ExchangeRateProvider provider, CurrencyUnit baseCurrency, LocalDate localDate) {
        return withSlidingWindow(provider, baseCurrency, localDate, 4);
    }

    static ExchangeRateResponseTransformer withSlidingWindow(ExchangeRateProvider provider, CurrencyUnit baseCurrency, LocalDate localDate, int days) {
        final LocalDate[] localDates = IntStream.range(0, days)
                .boxed()
                .map(localDate::minusDays)
                .sorted(Comparator.<LocalDate>naturalOrder().reversed())
                .toArray(LocalDate[]::new);

        return ExchangeRateResponseTransformer.builder()
                .provider(provider)
                .baseCurrency(baseCurrency)
                .localDates(localDates)
                .build();
    }

    private final ExchangeRateProvider provider;
    private final CurrencyUnit baseCurrency;
    private final LocalDate[] localDates;

    @Builder
    public ExchangeRateResponseTransformer(ExchangeRateProvider provider, CurrencyUnit baseCurrency, LocalDate... localDates) {
        requireNonNull(localDates);
        checkArgument(localDates.length > 0, "At least one date must be provided");

        this.localDates = localDates;
        this.provider = requireNonNull(provider);
        this.baseCurrency = requireNonNull(baseCurrency);
    }

    @Override
    public Publisher<ExchangeRateResponse> apply(Flux<CurrencyUnit> currencyUnitFlux) {
        return currencyUnitFlux.filter(currency -> provider.isAvailable(baseCurrency, currency))
                .map(currency -> ConversionQueryBuilder.of()
                        .setBaseCurrency(baseCurrency)
                        .setTermCurrency(currency)
                        .set(localDates)
                        .build())
                .flatMap(conversionQuery -> {
                    try {
                        return Flux.just(provider.getExchangeRate(conversionQuery));
                    } catch (Exception e) {
                        return Flux.empty();
                    }
                })
                .map(exchangeRate -> ExchangeRateResponseImpl.ExchangeRateImpl.builder()
                        .factor(exchangeRate.getFactor().doubleValueExact())
                        .base(exchangeRate.getBaseCurrency().getCurrencyCode())
                        .target(exchangeRate.getCurrency().getCurrencyCode())
                        .provider(exchangeRate.getContext().getProviderName())
                        .derived(exchangeRate.isDerived())
                        .type(exchangeRate.getContext().getRateType().name())
                        .build())
                .collectList()
                .map(rates -> ExchangeRateResponseImpl.builder()
                        .base(baseCurrency.getCurrencyCode())
                        .date(localDates[0])
                        .rates(rates)
                        .build());
    }
}
