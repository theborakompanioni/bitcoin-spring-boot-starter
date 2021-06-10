package org.tbk.bitcoin.example.payreq.invoice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface Invoices extends JpaRepository<Invoice, UUID> {
}
