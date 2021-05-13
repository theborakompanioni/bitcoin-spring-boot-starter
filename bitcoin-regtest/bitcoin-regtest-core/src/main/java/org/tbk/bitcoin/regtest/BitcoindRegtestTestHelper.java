package org.tbk.bitcoin.regtest;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
public final class BitcoindRegtestTestHelper {

    // TODO: consider moving class to test jar and wrap in spring test execution listner or junit extension?
    public static synchronized void createDefaultWalletIfNecessary(BitcoinClient bitcoinJsonRpcClient) throws IOException {
        int bitcoinCoreVersion = bitcoinJsonRpcClient.getNetworkInfo().getVersion();

        // bitcoin core 0.21+ will not create default wallet (named "") if it doesn't exist - lets create it
        // this prevents errors for certain operations that need a loaded wallet
        // e.g. "generatetoaddress" would result in error:
        // "No wallet is loaded. Load a wallet using loadwallet or create a new one with createwallet."
        boolean shouldCreateDefaultWallet = bitcoinCoreVersion >= 210000;
        if (shouldCreateDefaultWallet) {
            String defaultWalletName = ""; // default wallet is named ""
            List<String> walletList = bitcoinJsonRpcClient.listWallets();
            if (!walletList.contains(defaultWalletName)) {
                Map<String, String> result = bitcoinJsonRpcClient.createWallet(defaultWalletName, false, false);
                log.warn("Created default wallet: {}", result);
            }
        }
    }

    private BitcoindRegtestTestHelper() {
    }
}
