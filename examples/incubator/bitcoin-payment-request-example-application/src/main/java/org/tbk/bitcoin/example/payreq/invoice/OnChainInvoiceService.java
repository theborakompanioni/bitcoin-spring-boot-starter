package org.tbk.bitcoin.example.payreq.invoice;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bitcoinj.core.Address;
import org.springframework.transaction.annotation.Transactional;
import org.tbk.bitcoin.example.payreq.common.Network;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
class OnChainInvoiceService implements InvoiceService {

    @NonNull
    private final BitcoinClient bitcoinClient;

    @NonNull
    private final Invoices invoiceRequestRepository;

    @Override
    @Transactional
    public Invoice create(Invoice.InvoiceBuilder prototype) {

        Instant now = Instant.now();
        Instant validUntil = now.plus(5, ChronoUnit.MINUTES);

        Invoice.InvoiceBuilder builder = prototype
                .createdAt(now.toEpochMilli())
                .validUntil(validUntil.toEpochMilli());

        try {
            Address newAddress = bitcoinClient.getNewAddress();
            builder.network(Network.fromNetworkParameters(newAddress.getParameters()).name());
            builder.comment(newAddress.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Invoice invoice = builder.build();

        invoice.created();

        return invoiceRequestRepository.saveAndFlush(invoice);
    }
}
