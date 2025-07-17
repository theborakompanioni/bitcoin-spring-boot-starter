package org.tbk.electrum;

import lombok.Builder;
import lombok.Value;
import org.tbk.electrum.model.*;
import org.tbk.electrum.rpc.command.*;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

public interface ElectrumClient extends AutoCloseable {

    default boolean isConnected() {
        return this.getInfo().isConnected();
    }

    default Seed createMnemonicSeed() {
        return createMnemonicSeed(MakeSeedParams.builder().build());
    }

    Seed createMnemonicSeed(MakeSeedParams params);

    Wallet createWallet(CreateParams params);

    Seed getMnemonicSeed(GetSeedParams params);

    default Boolean isWalletSynchronized() {
        return isWalletSynchronized(IsSynchronizedParams.builder().build());
    }

    Boolean isWalletSynchronized(IsSynchronizedParams params);

    List<ListWalletEntry> listOpenWallets();

    default Balance getBalance() {
        return getBalance(GetBalanceParams.builder().build());
    }

    Balance getBalance(GetBalanceParams params);

    default List<String> listAddresses() {
        return listAddresses(ListAddressOptions.all());
    }

    List<String> listAddresses(ListAddressOptions options);

    List<String> listAddressesFunded();

    List<String> listAddressesUnfunded();

    default List<AddressWithBalance> listAddressesWithBalance() {
        return listAddressesWithBalance(ListAddressOptions.all());
    }

    List<AddressWithBalance> listAddressesWithBalance(ListAddressOptions options);

    void setLabel(SetLabelParams params);

    default Boolean isOwnerOfAddress(String address) {
        return isOwnerOfAddress(IsMineParams.builder()
                .address(address)
                .build());
    }

    Boolean isOwnerOfAddress(IsMineParams params);

    default Optional<String> getUnusedAddress() {
        return getUnusedAddress(GetUnusedAddressParams.builder().build());
    }

    Optional<String> getUnusedAddress(GetUnusedAddressParams params);

    default String createNewAddress() {
        return createNewAddress(CreateNewAddressParams.builder().build());
    }

    String createNewAddress(CreateNewAddressParams params);

    Balance getAddressBalance(String address);

    Utxos getAddressUnspent(String address);

    Utxos getUtxos(ListUnspentParams params);

    List<TxHashAndBlockHeight> getAddressHistory(String address);

    OnchainHistory getOnchainHistory();

    default OnchainSummary getOnchainCapitalGains() {
        return getOnchainCapitalGains(OnchainCapitalGainsParams.builder().build());
    }

    OnchainSummary getOnchainCapitalGains(OnchainCapitalGainsParams params);


    RawTx getRawTransaction(String txHash);

    Tx getDeserializedTransaction(String txHash);

    Tx getDeserializedTransaction(RawTx rawTx);

    GetInfoResponse getInfo();

    Feerate getFeerate();

    boolean changeGapLimit(ChangeGapLimitParams params);

    default int getMinAcceptableGap() {
        return getMinAcceptableGap(GetMinAcceptableGapParams.builder().build());
    }

    int getMinAcceptableGap(GetMinAcceptableGapParams params);

    boolean loadWallet(LoadWalletParams params);

    boolean unlockWallet(UnlockWalletParams params);

    Boolean closeWallet(CloseWalletParams params);

    RestoreResponse restoreWallet(RestoreParams params);

    List<String> listConfigKeys();

    default Optional<Object> getConfig(ConfigKeyEnum key) {
        return getConfig(key.getKey());
    }

    default Optional<Object> getConfig(ConfigKey key) {
        return getConfig(key.getKey());
    }

    Optional<Object> getConfig(String key);

    default void setConfig(ConfigKeyEnum key, String value) {
        setConfig(key.getKey(), value);
    }

    default void setConfig(ConfigKey key, String value) {
        setConfig(key.getKey(), value);
    }

    void setConfig(String key, String value);

    default void unsetConfig(ConfigKeyEnum key) {
        unsetConfig(key.getKey());
    }

    default void unsetConfig(ConfigKey key) {
        unsetConfig(key.getKey());
    }

    void unsetConfig(String key);

    RawTx createTransaction(PaytoParams params);

    RawTx createUnsignedTransactionSendingEntireBalance(String destinationAddress);

    RawTx createUnsignedTransactionSendingEntireBalance(String destinationAddress, TxoValue fee);

    RawTx createUnsignedTransaction(TxoValue value,
                                    String destinationAddress,
                                    String changeAddress,
                                    TxoValue fee,
                                    String walletPath,
                                    String password);

    RawTx createUnsignedTransaction(TxoValue value,
                                    String destinationAddress,
                                    String changeAddress);

    /**
     * Sign an unsigned transaction.
     *
     * @param params an unsigned transaction and wallet details
     * @return a signed transaction
     */
    RawTx signTransaction(SignTransactionParams params);

    String broadcast(RawTx rawTx);

    Boolean addAddressChangedNotificationCallback(String address, URI url);

    Boolean removeAddressChangedNotificationCallback(String address);

    List<String> getPublicKeys(String address);

    String encryptMessage(String publicKeyHex, String plaintext);

    String decryptMessage(DecryptParams params);

    String signMessage(String address, String message, @Nullable String walletPassphrase);

    Boolean verifyMessage(String address, String signature, String message);

    Version daemonVersion();

    Map<String, String> daemonVersionInfo();

    Boolean isValidAddress(String firstAddress);

    Future<?> waitForWalletSynchronization();

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
        Boolean frozen;
        @Nullable
        Boolean unused;
        @Nullable
        Boolean funded;
    }
}
