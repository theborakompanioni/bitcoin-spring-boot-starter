package org.tbk.bitcoin.example.payreq.donation;

import org.jmolecules.ddd.integration.AssociationResolver;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface Donations extends CrudRepository<Donation, Donation.DonationId>,
        PagingAndSortingRepository<Donation, Donation.DonationId>,
        AssociationResolver<Donation, Donation.DonationId> {
}
