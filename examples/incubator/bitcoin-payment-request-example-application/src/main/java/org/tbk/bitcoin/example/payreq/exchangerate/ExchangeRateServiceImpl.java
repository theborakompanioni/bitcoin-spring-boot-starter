package org.tbk.bitcoin.example.payreq.exchangerate;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
class ExchangeRateServiceImpl implements ExchangeRateService {

    @NonNull
    private final ExchangeRates exchangeRates;

    @TransactionalEventListener
    void on(ExchangeRate.ExchangeRateCreatedEvent event) {
        ExchangeRate domain = exchangeRates.findById(event.getDomainId())
                .orElseThrow(() -> new EmptyResultDataAccessException(1));

        log.info("Received application event: {}", domain);
    }
}
