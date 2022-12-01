package org.tbk.bitcoin.exchange.example.api.rate;

import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tbk.bitcoin.exchange.example.api.rate.ExchangeRateResponseImpl.ExchangeRateImpl;
import reactor.core.publisher.Flux;

import javax.money.CurrencyUnit;
import javax.money.MonetaryException;
import javax.money.convert.ConversionQueryBuilder;
import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.MonetaryConversions;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/exchange", produces = MediaType.APPLICATION_JSON_VALUE)
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
            @Parameter(array = @ArraySchema(schema = @Schema(implementation = String.class)))
            @RequestParam(name = "target", required = false) List<CurrencyUnit> targetParamOrNull,
            @RequestParam(name = "provider", required = false) List<String> providerParamOrNull
    ) {
        checkArgument(baseParamOrNull != null, "'base' must not be empty");

        CurrencyUnit baseCurrency = requireNonNull(baseParamOrNull);

        List<CurrencyUnit> targetCurrencies = Optional.ofNullable(targetParamOrNull)
                .orElseGet(Collections::emptyList);

        List<ExchangeRateProvider> exchangeRateProviders = Optional.ofNullable(providerParamOrNull).stream()
                .flatMap(Collection::stream)
                .map(MonetaryConversions::getExchangeRateProvider)
                .toList();

        ConversionQueryBuilder conversionQueryBuilder = ConversionQueryBuilder.of()
                .setBaseCurrency(baseCurrency);

        Optional<ExchangeRateResponseImpl> exchangeRateResponse = Flux.fromIterable(targetCurrencies)
                .map(conversionQueryBuilder::setTermCurrency)
                .map(ConversionQueryBuilder::build)
                .flatMap(conversionQuery -> {
                    Flux<ExchangeRateProvider> providers = Optional.of(exchangeRateProviders)
                            .filter(val -> !val.isEmpty())
                            .map(Flux::fromIterable)
                            .orElseGet(() -> Flux.fromIterable(MonetaryConversions.getDefaultConversionProviderChain())
                                    .map(MonetaryConversions::getExchangeRateProvider));

                    return providers
                            .filter(provider -> provider.isAvailable(conversionQuery))
                            .flatMap(provider -> {
                                try {
                                    return Flux.just(provider.getExchangeRate(conversionQuery));
                                } catch (Exception e) {
                                    log.debug("exception while getting exchange rate {} from provider {}: {}",
                                            conversionQuery, provider, e.getMessage());

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
            boolean providerNotFound = e.getMessage().startsWith("No such rate provider:")
                    || e.getMessage().startsWith("Invalid ExchangeRateProvider (not found):");

            if (providerNotFound) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}
