package org.tbk.bitcoin.example.payreq.invoice;

import org.tbk.bitcoin.example.payreq.invoice.api.query.InvoiceForm;

public interface InvoiceService {
    Invoice create(InvoiceForm prototype);
}
