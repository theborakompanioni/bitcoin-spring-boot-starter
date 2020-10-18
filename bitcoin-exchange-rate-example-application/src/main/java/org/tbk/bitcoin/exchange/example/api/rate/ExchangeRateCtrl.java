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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;


@RestController
@RequestMapping(value = "/api/v1/exchange")
@RequiredArgsConstructor
public class ExchangeRateCtrl {

    private static final CurrencyUnit DEFAULT_CURRENCY_UNIT = Monetary.getCurrency("EUR");

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
            @RequestParam(name = "provider", required = false) String providerParamOrNull,
            @RequestParam(name = "days", required = false, defaultValue = "1") Integer daysParam
    ) {
        checkArgument(daysParam > 0, "'days' must be positive");

        CurrencyUnit baseCurrency = Optional.ofNullable(baseParamOrNull)
                .orElse(DEFAULT_CURRENCY_UNIT);

        List<CurrencyUnit> targetCurrencies = Optional.ofNullable(targetParamOrNull)
                .orElseGet(Collections::emptyList);

        Optional<ExchangeRateProvider> exchangeRateProvider = Optional.ofNullable(providerParamOrNull)
                .map(MonetaryConversions::getExchangeRateProvider);

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
                    try {
                        ExchangeRateProvider provider = exchangeRateProvider
                                .orElseGet(() -> MonetaryConversions.getExchangeRateProvider(conversionQuery));
                        return Flux.just(provider.getExchangeRate(conversionQuery));
                    } catch (Exception e) {
                        return Flux.empty();
                    }
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
