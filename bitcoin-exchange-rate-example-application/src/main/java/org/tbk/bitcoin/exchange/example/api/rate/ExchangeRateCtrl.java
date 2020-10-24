package org.tbk.bitcoin.exchange.example.api.rate;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tbk.bitcoin.exchange.example.api.rate.ExchangeRateResponseImpl.ExchangeRateImpl;
import reactor.core.publisher.Flux;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryException;
import javax.money.convert.ConversionQueryBuilder;
import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.MonetaryConversions;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;


@RestController
@RequestMapping(value = "/api/v1/exchange")
@RequiredArgsConstructor
public class ExchangeRateCtrl {

    @GetMapping
    public ResponseEntity<? extends Map<String, Object>> get() {
        Map<String, Object> result = ImmutableMap.<String, Object>builder()
                .put("providerNames", MonetaryConversions.getConversionProviderNames())
                .put("providerChain", MonetaryConversions.getDefaultConversionProviderChain())
                .build();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/latest")
    public ResponseEntity<ExchangeRateResponseImpl> latest(
            @RequestParam(name = "base", required = false) CurrencyUnit baseParamOrNull,
            @RequestParam(name = "target", required = false) List<CurrencyUnit> targetParamOrNull,
            @RequestParam(name = "provider", required = false) List<String> providerParamOrNull,
            @RequestParam(name = "days", required = false, defaultValue = "1") Integer daysParam
    ) {
        checkArgument(baseParamOrNull != null, "'base' must not be empty");
        checkArgument(daysParam > 0, "'days' must be positive");

        CurrencyUnit baseCurrency = requireNonNull(baseParamOrNull);

        List<CurrencyUnit> targetCurrencies = Optional.ofNullable(targetParamOrNull)
                .orElseGet(Collections::emptyList);

        List<ExchangeRateProvider> exchangeRateProviders = Optional.ofNullable(providerParamOrNull).stream()
                .flatMap(Collection::stream)
                .map(MonetaryConversions::getExchangeRateProvider)
                .collect(Collectors.toList());

        LocalDate[] dates = SlidingWindows.withSlidingWindow(LocalDate.now(), daysParam)
                .toArray(LocalDate[]::new);

        ConversionQueryBuilder conversionQueryBuilder = ConversionQueryBuilder.of()
                .setBaseCurrency(baseCurrency)
                //.set(dates)
                ;

        Optional<ExchangeRateResponseImpl> exchangeRateResponse = Flux.fromIterable(targetCurrencies)
                .map(conversionQueryBuilder::setTermCurrency)
                .map(ConversionQueryBuilder::build)
                .flatMap(conversionQuery -> {
                    Flux<ExchangeRateProvider> provider = Optional.of(exchangeRateProviders)
                            .filter(val -> !val.isEmpty())
                            .map(Flux::fromIterable)
                            .orElseGet(() -> Flux.fromIterable(MonetaryConversions.getDefaultConversionProviderChain())
                                    .map(MonetaryConversions::getExchangeRateProvider));

                    return provider.flatMap(val -> {
                        try {
                            return Flux.just(val.getExchangeRate(conversionQuery));
                        } catch (Exception e) {
                            return Flux.empty();
                        }
                    });
                })
                .map(exchangeRate -> ExchangeRateImpl.toDto(exchangeRate).build())
                .collectList()
                .map(rates -> ExchangeRateResponseImpl.builder()
                        .base(baseCurrency.getCurrencyCode())
                        .rates(rates)
                        .build())
                .blockOptional();

        return ResponseEntity.of(exchangeRateResponse);
    }


    @GetMapping("/provider")
    public ResponseEntity<ExchangeRateProvider> provider(@RequestParam("name") String providerParam) {
        try {
            ExchangeRateProvider exchangeRateProvider = MonetaryConversions.getExchangeRateProvider(providerParam);
            return ResponseEntity.ok(exchangeRateProvider);
        } catch (MonetaryException e) {
            boolean providerNotFound = e.getMessage().startsWith("No such rate provider:") ||
                    e.getMessage().startsWith("Invalid ExchangeRateProvider (not found):");

            if (providerNotFound) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}
