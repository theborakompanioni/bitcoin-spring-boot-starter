package org.tbk.bitcoin.example.payreq.invoice;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
class InvoiceModuleConfig {

    @Bean
    public OnChainInvoiceService onChainInvoiceService(BitcoinClient bitcoinClient,
                                                       Invoices invoiceRequestRepository) {
        return new OnChainInvoiceService(bitcoinClient, invoiceRequestRepository);
    }

    @Bean
    public InitializingBean walletInitializer(BitcoinClient bitcoinClient) {
        return () -> {
            List<String> wallets = bitcoinClient.listWallets();
            if (wallets.isEmpty()) {
                Map<String, String> wallet = bitcoinClient.createWallet("tbk-bitcoin-spring-boot-starter", false, false);
                log.info("wallet created: {}", wallet);
            }
        };
    }
}
