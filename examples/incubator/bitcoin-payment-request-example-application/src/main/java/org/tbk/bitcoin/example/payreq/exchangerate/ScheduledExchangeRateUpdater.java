package org.tbk.bitcoin.example.payreq.exchangerate;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;
import org.tbk.bitcoin.example.payreq.common.Currencies;

import javax.money.CurrencyUnit;
import javax.money.NumberValue;
import javax.money.convert.ConversionQuery;
import javax.money.convert.ConversionQueryBuilder;
import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.RateType;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
class ScheduledExchangeRateUpdater implements ApplicationContextAware, DisposableBean {

    @NonNull
    private final ExchangeRates exchangeRates;

    @NonNull
    private final TransactionTemplate transactionTemplate;

    private final AtomicReference<ApplicationContext> applicationContext = new AtomicReference<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext.set(applicationContext);
    }

    @Override
    public void destroy() {
        // reset application context - avoid accessing beans during shutdown phase
        this.applicationContext.set(null);
    }

    @Scheduled(initialDelay = 0, fixedDelay = 120_000)
    public void fetchExchangeRatesOneIteration() {
        fetchExchangeRates();
    }

    private void fetchExchangeRates() {
        Collection<ExchangeRateProvider> exchangeRateProviders = exchangeRateProviders();
        if (exchangeRateProviders.isEmpty()) {
            log.warn("No ExchangeRateProvider found.");
            return;
        }

        log.debug("======================================================");
        log.debug("Available provider count: {}", exchangeRateProviders.size());

        List<CurrencyUnit> fiatCurrencyUnits = List.of(Currencies.EUR, Currencies.USD);

        fiatCurrencyUnits.forEach(fiatCurrencyUnit -> {
            log.debug("trying to load exchange rate for {}/{}", fiatCurrencyUnit, Currencies.BTC);
            loadExchangeRate(exchangeRateProviders, Currencies.BTC, fiatCurrencyUnit);
        });
        log.debug("======================================================");
    }

    private Collection<ExchangeRateProvider> exchangeRateProviders() {
        return Optional.ofNullable(applicationContext.get())
                .map(it -> it.getBeansOfType(ExchangeRateProvider.class))
                .map(Map::values)
                .orElseGet(Collections::emptyList);
    }

    private ExchangeRate createEntity(javax.money.convert.ExchangeRate exchangeRate) {
        String providerName = exchangeRate.getContext().getProviderName();
        RateType rateType = exchangeRate.getContext().getRateType();
        NumberValue factor = exchangeRate.getFactor();
        ExchangeRate entity = new ExchangeRate(providerName, rateType, exchangeRate.getBaseCurrency(), exchangeRate.getCurrency(), factor);

        return entity;
    }

    private void loadExchangeRate(Collection<ExchangeRateProvider> exchangeRateProviders,
                                  CurrencyUnit base,
                                  CurrencyUnit term) {

        ConversionQueryBuilder conversionQueryBuilder = ConversionQueryBuilder.of()
                .setBaseCurrency(base)
                .setTermCurrency(term);

        ConversionQuery conversionQuery = conversionQueryBuilder.build();

        log.debug("------------------------------------------------------");
        log.debug("ConversionQuery: {}", conversionQuery);

        List<ExchangeRateProvider> eligibleProvider = exchangeRateProviders.stream()
                .filter(it -> it.isAvailable(conversionQuery))
                .collect(Collectors.toList());

        log.debug("Eligible provider count: {}", eligibleProvider.size());

        eligibleProvider.forEach(xChangeExchangeRateProvider -> {
            loadExchangeRate(xChangeExchangeRateProvider, conversionQuery);
        });
        log.debug("------------------------------------------------------");
    }

    private void loadExchangeRate(ExchangeRateProvider exchangeRateProvider,
                                  ConversionQuery conversionQuery) {
        log.debug("------------------------------------------------------");
        log.debug("Provider: {}", exchangeRateProvider.getContext());

        javax.money.convert.ExchangeRate exchangeRate = exchangeRateProvider.getExchangeRate(conversionQuery);

        transactionTemplate.executeWithoutResult(tx -> {
            ExchangeRate entity = createEntity(exchangeRate);
            exchangeRates.save(entity);
        });

        log.debug("exchangeRate: {}", exchangeRate);
    }
}
