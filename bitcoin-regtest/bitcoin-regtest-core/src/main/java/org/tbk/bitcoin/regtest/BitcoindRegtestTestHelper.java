package org.tbk.bitcoin.regtest;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;


// TODO: consider moving class to test jar and wrap in spring test execution listener or junit extension?
@Slf4j
public final class BitcoindRegtestTestHelper {
    private static final Duration WAIT_FOR_SERVER_TIMEOUT = Duration.ofSeconds(30);

    public static void createLegacyDefaultWalletIfNecessary(BitcoinClient bitcoinJsonRpcClient) throws IOException {
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
            createWalletIfNecessary(bitcoinJsonRpcClient, defaultWalletName, false);
        }
    }

    public static void createDescriptorWallet(BitcoinClient bitcoinJsonRpcClient, String walletName) throws IOException {
        boolean ready = bitcoinJsonRpcClient.waitForServer((int) WAIT_FOR_SERVER_TIMEOUT.toSeconds());
        if (!ready) {
            throw new IllegalStateException("Server is not ready");
        }

        createWalletIfNecessary(bitcoinJsonRpcClient, walletName, true);
    }

    private static synchronized void createWalletIfNecessary(BitcoinClient bitcoinJsonRpcClient, String walletName, boolean descriptors) throws IOException {
            List<String> walletList = bitcoinJsonRpcClient.listWallets();
            if (!walletList.contains(walletName)) {
                // args for call to "createwallet". See https://developer.bitcoin.org/reference/rpc/createwallet.html
                ImmutableMap<String, Optional<Object>> argsMap = ImmutableMap.<String, Optional<Object>>builder()
                        .put("wallet_name", Optional.of(walletName))
                        .put("disable_private_keys", Optional.of(false))
                        .put("blank", Optional.of(false))
                        .put("passphrase", Optional.empty())
                        .put("avoid_reuse", Optional.of(false))
                        .put("descriptors", Optional.of(descriptors))
                        .put("load_on_startup", Optional.empty())
                        .build();

                Object[] args = argsMap.values().stream().map(it -> it.orElse(null)).toArray();
                Map<String, String> result = bitcoinJsonRpcClient.send("createwallet", args);
                log.warn("Created default wallet: {}", result);
            }
    }

    private BitcoindRegtestTestHelper() {
    }
}
