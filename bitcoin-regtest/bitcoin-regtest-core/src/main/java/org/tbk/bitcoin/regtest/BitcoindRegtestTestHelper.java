package org.tbk.bitcoin.regtest;

import com.google.common.collect.ImmutableMap;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class BitcoindRegtestTestHelper {
    private static final Duration WAIT_FOR_SERVER_TIMEOUT = Duration.ofSeconds(30);

    // TODO: consider moving class to test jar and wrap in spring test execution listener or junit extension?
    public static synchronized void createDefaultWalletIfNecessary(BitcoinClient bitcoinJsonRpcClient) throws IOException {
        boolean ready = bitcoinJsonRpcClient.waitForServer((int) WAIT_FOR_SERVER_TIMEOUT.toSeconds());
        if (!ready) {
            throw new IllegalStateException("Server is not ready");
        }

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
                // args for call to "createwallet". See https://developer.bitcoin.org/reference/rpc/createwallet.html
                ImmutableMap<String, Optional<Object>> argsMap = ImmutableMap.<String, Optional<Object>>builder()
                        .put("wallet_name", Optional.of(defaultWalletName))
                        .put("disable_private_keys", Optional.of(false))
                        .put("blank", Optional.of(false))
                        .put("passphrase", Optional.empty())
                        .put("avoid_reuse", Optional.of(false))
                        // creating descriptors wallets needs bitcoind compiled with sqlite3 support,
                        // which is unknown here and hence disabled
                        // (e.g. docker image for tests does not support it)
                        .put("descriptors", Optional.of(false))
                        .put("load_on_startup", Optional.empty())
                        .build();

                Object[] args = argsMap.values().stream().map(it -> it.orElse(null)).toArray();
                Map<String, String> result = bitcoinJsonRpcClient.send("createwallet", args);
                log.warn("Created default wallet: {}", result);
            }
        }
    }

    private BitcoindRegtestTestHelper() {
    }
}
