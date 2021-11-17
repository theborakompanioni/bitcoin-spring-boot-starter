package org.tbk.spring.testcontainer.bitcoind.example;

import com.msgilligan.bitcoinj.rpc.BitcoinExtendedClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.regtest.BitcoindRegtestTestHelper;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class BitcoindContainerExampleApplicationConfig {
    /**
     * We must have access to a wallet for "getnewaddress" command to work.
     * Create a wallet if none is found (currently only when in regtest mode)!
     * Maybe move to {@link org.tbk.bitcoin.regtest.config.BitcoinRegtestAutoConfiguration}?
     */
    @Bean
    public InitializingBean createWalletIfMissing(BitcoinExtendedClient bitcoinRegtestClient) {
        return () -> BitcoindRegtestTestHelper.createDefaultWalletIfNecessary(bitcoinRegtestClient);
    }
}
