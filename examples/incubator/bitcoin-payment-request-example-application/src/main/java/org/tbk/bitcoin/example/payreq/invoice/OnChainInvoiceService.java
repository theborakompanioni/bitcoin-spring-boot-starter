package org.tbk.bitcoin.example.payreq.invoice;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bitcoinj.core.Address;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbk.bitcoin.example.payreq.common.Network;
import org.tbk.bitcoin.example.payreq.invoice.api.query.InvoiceForm;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
class OnChainInvoiceService implements InvoiceService {

    @NonNull
    private final BitcoinClient bitcoinClient;

    @NonNull
    private final Invoices invoices;

    @Override
    @Transactional
    public Invoice create(InvoiceForm form) {
        Network network = form.getNetwork()
                .map(Network::valueOf)
                .orElse(Network.mainnet);

        boolean networkSupported = bitcoinClient.getNetParams().equals(network.toNetworkParameters());
        if (!networkSupported) {
            throw new IllegalArgumentException("Network not supported");
        }

        Instant now = Instant.now();
        Instant validUntil = now.plus(5, ChronoUnit.MINUTES);

        Invoice invoice = new Invoice(validUntil);
        invoice.setComment(form.getComment().orElse(null));

        try {
            Address newAddress = bitcoinClient.getNewAddress();
            invoice.setNetwork(Network.fromNetworkParameters(newAddress.getParameters()).name());
            invoice.setComment(newAddress.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return invoices.save(invoice);
    }
}
