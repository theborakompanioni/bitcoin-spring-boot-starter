package org.tbk.bitcoin.example.payreq.bitcoin.block;

import org.jmolecules.spring.AssociationResolver;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BitcoinBlocks extends PagingAndSortingRepository<BitcoinBlock, BitcoinBlock.BitcoinBlockId>,
        AssociationResolver<BitcoinBlock, BitcoinBlock.BitcoinBlockId>,
        JpaSpecificationExecutor<BitcoinBlock> {
}
