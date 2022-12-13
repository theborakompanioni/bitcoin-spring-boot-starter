package org.tbk.bitcoin.example.payreq.donation.api;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.tbk.bitcoin.example.payreq.donation.Donation;
import org.tbk.bitcoin.example.payreq.donation.Donations;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/donation")
@RequiredArgsConstructor
public class DonationApi {

    @NonNull
    private final Donations donations;

    @Transactional
    @GetMapping(path = "/{id}")
    public ResponseEntity<Donation> get(@PathVariable UUID id) {
        var donationId = Donation.DonationId.of(id.toString());

        Donation entity = donations.findById(donationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found"));

        return ResponseEntity.ok(entity);
    }
}
