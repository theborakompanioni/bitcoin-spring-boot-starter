package org.tbk.electrum;

import lombok.Builder;
import lombok.Value;
import org.tbk.electrum.command.DaemonCloseWalletRequest;
import org.tbk.electrum.command.DaemonLoadWalletRequest;
import org.tbk.electrum.command.DaemonStatusResponse;
import org.tbk.electrum.model.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public interface ElectrumClient {

    default boolean isDaemonConnected() {
        return this.daemonStatus().isConnected();
    }

    Version daemonVersion();

    List<String> createMnemonicSeed();

    Boolean isWalletSynchronized();

    Balance getBalance();

    List<String> listAddresses();

    List<String> listAddresses(ListAddressOptions options);

    List<String> listAddressesFunded();

    List<String> listAddressesUnfunded();

    Boolean isOwnerOfAddress(String address);

    Optional<String> getUnusedAddress();

    String createNewAddress();

    Balance getAddressBalance(String address);

    Utxos getAddressUnspent(String address);

    List<TxHashAndBlockHeight> getAddressHistory(String address);

    History getHistory();

    RawTx getRawTransaction(String txHash);

    Tx getDeserializedTransaction(String txHash);

    Tx getDeserializedTransaction(RawTx rawTx);

    DaemonStatusResponse daemonStatus();

    Boolean loadWallet(DaemonLoadWalletRequest request);

    Boolean closeWallet(DaemonCloseWalletRequest request);

    List<String> getMnemonicSeed(String walletPassphrase);

    Optional<Object> daemonGetConfig(ConfigKey key);

    void daemonSetConfig(ConfigKey key, String value);

    RawTx createUnsignedTransactionSendingEntireBalance(String destinationAddress);

    RawTx createUnsignedTransactionSendingEntireBalance(String destinationAddress, TxoValue fee);

    RawTx createUnsignedTransaction(TxoValue value,
                                    String destinationAddress,
                                    String changeAddress,
                                    TxoValue fee);

    RawTx createUnsignedTransaction(TxoValue value,
                                    String destinationAddress,
                                    String changeAddress);

    /**
     * Sign an unsigned transaction.
     *
     * @param rawTx            an unsigned transaction
     * @param walletPassphrase the wallet password or null if wallet is not encrypted
     * @return a signed transaction
     */
    RawTx signTransaction(RawTx rawTx, @Nullable String walletPassphrase);

    String broadcast(RawTx rawTx);

    Boolean addAddressChangedNotificationCallback(String address, String url);

    List<String> getPublicKeys(String address);

    String encryptMessage(String publicKeyHex, String plaintext);

    String decryptMessage(String publicKeyHex, String encryptedMessage, @Nullable String walletPassphrase);

    String signMessage(String address, String message, @Nullable String walletPassphrase);

    Boolean verifyMessage(String address, String signature, String message);

    @Value
    @Builder
    class ListAddressOptions {
        private static final ListAddressOptions ALL = builder().build();

        public static ListAddressOptions all() {
            return ALL;
        }

        @Nullable
        Boolean receiving;
        @Nullable
        Boolean change;
        @Nullable
        Boolean labels;
        @Nullable
        Boolean frozen;
        @Nullable
        Boolean unused;
        @Nullable
        Boolean funded;
        @Nullable
        Boolean balance;
    }
}
