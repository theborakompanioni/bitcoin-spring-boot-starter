package org.tbk.bitcoin.example.payreq.bitcoin.chaininfo;

import org.jmolecules.spring.AssociationResolver;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BitcoinChainInfos extends CrudRepository<BitcoinChainInfo, BitcoinChainInfo.BitcoinChainInfoId>,
        PagingAndSortingRepository<BitcoinChainInfo, BitcoinChainInfo.BitcoinChainInfoId>,
        AssociationResolver<BitcoinChainInfo, BitcoinChainInfo.BitcoinChainInfoId> {
}
