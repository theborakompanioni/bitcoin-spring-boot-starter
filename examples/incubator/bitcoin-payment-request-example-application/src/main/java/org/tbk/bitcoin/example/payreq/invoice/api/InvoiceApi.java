package org.tbk.bitcoin.example.payreq.invoice.api;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.tbk.bitcoin.example.payreq.invoice.Invoice;
import org.tbk.bitcoin.example.payreq.invoice.Invoices;

import javax.transaction.Transactional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoice")
@RequiredArgsConstructor
public class InvoiceApi {

    @NonNull
    private final Invoices invoiceRequestRepository;

    @Transactional
    @GetMapping(path = "/{id}")
    public ResponseEntity<Invoice> get(@PathVariable UUID id) {
        Invoice entity = invoiceRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found"));

        return ResponseEntity.ok(entity);
    }
}
