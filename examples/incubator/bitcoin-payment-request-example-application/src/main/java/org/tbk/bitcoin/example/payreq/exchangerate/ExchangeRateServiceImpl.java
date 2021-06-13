package org.tbk.bitcoin.example.payreq.exchangerate;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.context.event.EventListener;

@Slf4j
@Service
@RequiredArgsConstructor
class ExchangeRateServiceImpl implements ExchangeRateService {

    @NonNull
    private final ExchangeRates exchangeRates;

    @EventListener
    void on(ExchangeRate.ExchangeRateCreatedEvent event) {
        log.info("Received application event: {}", event.getDomain());
    }
}
