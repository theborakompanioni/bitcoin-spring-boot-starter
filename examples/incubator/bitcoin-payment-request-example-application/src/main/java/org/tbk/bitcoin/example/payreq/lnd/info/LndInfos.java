package org.tbk.bitcoin.example.payreq.lnd.info;

import org.jmolecules.spring.AssociationResolver;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

interface LndInfos extends CrudRepository<LndInfo, LndInfo.LndInfoId>,
        PagingAndSortingRepository<LndInfo, LndInfo.LndInfoId>,
        AssociationResolver<LndInfo, LndInfo.LndInfoId> {
}
