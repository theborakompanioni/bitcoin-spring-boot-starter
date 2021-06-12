package org.tbk.bitcoin.example.payreq.invoice.api;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.tbk.bitcoin.example.payreq.invoice.Invoice;
import org.tbk.bitcoin.example.payreq.invoice.InvoiceService;
import org.tbk.bitcoin.example.payreq.invoice.api.query.InvoiceForm;

@RestController
@RequestMapping("/api/v1/invoice")
@RequiredArgsConstructor
public class InvoiceFormApi {

    @NonNull
    private final InvoiceService invoiceService;

    @PostMapping(path = "/form", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RedirectView invoiceForm(@Validated InvoiceForm invoiceForm,
                                    RedirectAttributes attributes) {

        Invoice entity = invoiceService.create(invoiceForm);

        attributes.addAttribute("invoice_id", entity.getId().getId());

        return new RedirectView("/invoice.html");
    }
}
