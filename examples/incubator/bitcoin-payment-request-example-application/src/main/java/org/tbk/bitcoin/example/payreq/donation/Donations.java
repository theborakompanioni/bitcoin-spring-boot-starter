package org.tbk.bitcoin.example.payreq.donation;

import org.jmolecules.spring.AssociationResolver;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface Donations extends PagingAndSortingRepository<Donation, Donation.DonationId>, AssociationResolver<Donation, Donation.DonationId> {
}
