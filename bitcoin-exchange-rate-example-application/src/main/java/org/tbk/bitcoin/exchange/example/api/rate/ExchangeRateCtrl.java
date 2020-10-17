package org.tbk.bitcoin.exchange.example.api.rate;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.MonetaryConversions;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping(value = "/api/v1/exchange")
@RequiredArgsConstructor
public class ExchangeRateCtrl {

    private static final CurrencyUnit DEFAULT_CURRENCY_UNIT = Monetary.getCurrency("EUR");

    private final ExchangeRateProvider defaultRateProvider;

    @GetMapping
    public ResponseEntity<? extends Map<String, Object>> get() {
        Map<String, Object> result = ImmutableMap.<String, Object>builder()
                .put("providerNames", MonetaryConversions.getConversionProviderNames())
                .put("providerChain", MonetaryConversions.getDefaultConversionProviderChain())
                .build();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/latest")
    public ResponseEntity<ExchangeRateResponse> latest(
            @RequestParam(name = "base", required = false) CurrencyUnit baseParamOrNull,
            @RequestParam(name = "target", required = false) List<CurrencyUnit> targetParamOrNull,
            @RequestParam(name = "provider", required = false) String providerParamOrNull
    ) {
        CurrencyUnit baseCurrency = Optional.ofNullable(baseParamOrNull)
                .orElse(DEFAULT_CURRENCY_UNIT);

        List<CurrencyUnit> targetCurrencies = Optional.ofNullable(targetParamOrNull)
                .orElseGet(Collections::emptyList);

        ExchangeRateProvider exchangeRateProvider = Optional.ofNullable(providerParamOrNull)
                .map(MonetaryConversions::getExchangeRateProvider)
                .orElse(defaultRateProvider);

        ExchangeRateResponseTransformer toCurrentExchangeRate = ExchangeRateResponseTransformer
                .ofTodayWithSlidingWindow(exchangeRateProvider, baseCurrency);

        ExchangeRateResponse exchangeRateResponse = Flux.fromIterable(targetCurrencies)
                .transform(toCurrentExchangeRate)
                .blockFirst();

        return ResponseEntity.ok(exchangeRateResponse);
    }
}
