package org.tbk.bitcoin.example.payreq.bitcoin.chaininfo;

import org.jmolecules.spring.AssociationResolver;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BitcoinChainInfos extends PagingAndSortingRepository<BitcoinChainInfo, BitcoinChainInfo.BitcoinChainInfoId>, AssociationResolver<BitcoinChainInfo, BitcoinChainInfo.BitcoinChainInfoId> {
}
