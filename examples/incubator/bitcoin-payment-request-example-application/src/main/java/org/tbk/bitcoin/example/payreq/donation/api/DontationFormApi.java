package org.tbk.bitcoin.example.payreq.donation.api;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.tbk.bitcoin.example.payreq.donation.Donation;
import org.tbk.bitcoin.example.payreq.donation.DonationService;
import org.tbk.bitcoin.example.payreq.donation.api.query.DonationForm;

@RestController
@RequestMapping("/api/v1/donation")
@RequiredArgsConstructor
public class DontationFormApi {

    @NonNull
    private final DonationService donationService;

    @PostMapping(path = "/form", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RedirectView invoiceForm(@Validated DonationForm form,
                                    RedirectAttributes attributes) {

        Donation entity = donationService.create(form);

        attributes.addAttribute("donation_id", entity.getId().getId());

        return new RedirectView("/donation.html");
    }
}
