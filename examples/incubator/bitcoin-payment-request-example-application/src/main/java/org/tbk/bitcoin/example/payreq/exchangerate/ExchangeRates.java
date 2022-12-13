package org.tbk.bitcoin.example.payreq.exchangerate;

import org.jmolecules.spring.AssociationResolver;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ExchangeRates extends CrudRepository<ExchangeRate, ExchangeRate.ExchangeRateId>,
        PagingAndSortingRepository<ExchangeRate, ExchangeRate.ExchangeRateId>,
        AssociationResolver<ExchangeRate, ExchangeRate.ExchangeRateId> {
}
