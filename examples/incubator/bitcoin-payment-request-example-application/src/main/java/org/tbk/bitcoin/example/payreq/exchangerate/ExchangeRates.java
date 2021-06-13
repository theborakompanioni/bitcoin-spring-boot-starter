package org.tbk.bitcoin.example.payreq.exchangerate;

import org.jmolecules.spring.AssociationResolver;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ExchangeRates extends PagingAndSortingRepository<ExchangeRate, ExchangeRate.ExchangeRateId>, AssociationResolver<ExchangeRate, ExchangeRate.ExchangeRateId> {
}
