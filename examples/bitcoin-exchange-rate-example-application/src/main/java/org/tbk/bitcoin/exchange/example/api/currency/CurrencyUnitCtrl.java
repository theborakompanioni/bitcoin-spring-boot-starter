package org.tbk.bitcoin.exchange.example.api.currency;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.money.CurrencyQuery;
import javax.money.CurrencyQueryBuilder;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/currency")
@RequiredArgsConstructor
public class CurrencyUnitCtrl {

    @GetMapping
    public ResponseEntity<Collection<CurrencyUnit>> get(
            @RequestParam(name = "provider", required = false) List<String> providerParamOrNull
    ) {
        List<String> providerNames = Optional.ofNullable(providerParamOrNull)
                .orElseGet(Collections::emptyList);

        CurrencyQuery currencyQuery = CurrencyQueryBuilder.of()
                .setProviderNames(providerNames)
                .build();

        Collection<CurrencyUnit> currencies = Monetary.getCurrencies(currencyQuery);

        List<CurrencyUnit> sortedCurrencies = currencies.stream()
                .sorted()
                .collect(Collectors.toList());

        return ResponseEntity.ok(sortedCurrencies);
    }

    @GetMapping(path = "/provider")
    public ResponseEntity<? extends Map<String, Object>> provider() {
        Map<String, Object> result = ImmutableMap.<String, Object>builder()
                .put("currencyProviderNames", Monetary.getCurrencyProviderNames())
                .build();

        return ResponseEntity.ok(result);
    }
}
