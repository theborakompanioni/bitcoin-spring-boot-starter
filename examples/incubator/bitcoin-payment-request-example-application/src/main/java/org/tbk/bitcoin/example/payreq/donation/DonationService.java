package org.tbk.bitcoin.example.payreq.donation;

import org.tbk.bitcoin.example.payreq.donation.api.query.DonationForm;

public interface DonationService {
    Donation create(DonationForm from);
}
