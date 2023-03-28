package org.tbk.bitcoin.example.payreq.bitcoin.block;

import org.jmolecules.ddd.integration.AssociationResolver;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BitcoinBlocks extends CrudRepository<BitcoinBlock, BitcoinBlock.BitcoinBlockId>,
        PagingAndSortingRepository<BitcoinBlock, BitcoinBlock.BitcoinBlockId>,
        AssociationResolver<BitcoinBlock, BitcoinBlock.BitcoinBlockId>,
        JpaSpecificationExecutor<BitcoinBlock> {
}
