package org.tbk.bitcoin.example.payreq.invoice;

import org.jmolecules.spring.AssociationResolver;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.tbk.bitcoin.example.payreq.invoice.Invoice.InvoiceId;

public interface Invoices extends PagingAndSortingRepository<Invoice, InvoiceId>, AssociationResolver<Invoice, InvoiceId> {
}
