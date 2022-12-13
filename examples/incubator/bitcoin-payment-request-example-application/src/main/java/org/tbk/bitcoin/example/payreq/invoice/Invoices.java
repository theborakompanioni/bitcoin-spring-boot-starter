package org.tbk.bitcoin.example.payreq.invoice;

import org.jmolecules.spring.AssociationResolver;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface Invoices extends CrudRepository<Invoice, Invoice.InvoiceId>,
        PagingAndSortingRepository<Invoice, Invoice.InvoiceId>,
        AssociationResolver<Invoice, Invoice.InvoiceId> {
}
