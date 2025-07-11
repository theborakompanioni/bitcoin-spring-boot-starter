package org.tbk.electrum;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.tbk.electrum.model.*;
import org.tbk.electrum.rpc.ElectrumDaemonRpcService;
import org.tbk.electrum.rpc.command.*;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.util.concurrent.MoreExecutors.shutdownAndAwaitTermination;
import static java.util.Objects.requireNonNull;

@Slf4j
public class ElectrumClientImpl implements ElectrumClient {
    final static byte[] PSBT_MAGIC_BYTES = {'p', 's', 'b', 't', (byte) 0xff};
    private static final String PSBT_BASE64_PREFIX = Base64.getEncoder().encodeToString(PSBT_MAGIC_BYTES).replaceAll("=", "");

    private static boolean looksLikePsbt(String value) {
        return value.startsWith(PSBT_BASE64_PREFIX);
    }

    private static List<String> splitMnemonicSeed(String seed) {
        return Arrays.asList(seed.split(" "));
    }

    private final String serviceId = Integer.toHexString(System.identityHashCode(this));

    private final ExecutorService syncExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setNameFormat("electrum-sync-" + serviceId + "-%d")
            .setDaemon(true)
            .build());

    private final ElectrumDaemonRpcService delegate;

    public ElectrumClientImpl(ElectrumDaemonRpcService delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public Optional<Object> getConfig(String key) {
        return Optional.ofNullable(delegate.getconfig(key));
    }

    @Override
    public void setConfig(String key, String value) {
        delegate.setconfig(key, value);
    }

    @Override
    public void unsetConfig(String key) {
        delegate.unsetconfig(key);
    }

    @Override
    public RawTx createTransaction(PaytoParams params) {
        try {
            String payto = delegate.payto(
                    params.getDestination(),
                    params.getAmount(),
                    params.getFee(),
                    params.getFeeRate(),
                    params.getFromAddress(),
                    params.getFromCoins(),
                    params.getChangeAddress(),
                    params.getNoCheck(),
                    params.getUnsigned(),
                    params.getReplaceByFee(),
                    params.getLocktime(),
                    params.getAddTransaction(),
                    params.getPassword(),
                    params.getWalletPath()
            );

            // payto can be base64 or hex
            // hex: for finalized tx?
            // base64: for unsigned tx?
            byte[] raw = fromHexOrBase64(payto);
            boolean signed = !looksLikePsbt(payto);

            return SimpleRawTx.builder()
                    .hex(HexFormat.of().formatHex(raw))
                    .signed(signed)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Could not deserialize request");
        }
    }

    @Override
    public RawTx createUnsignedTransactionSendingEntireBalance(String destinationAddress) {
        return this.createTransaction(PaytoParams.builder()
                .destination(destinationAddress)
                .amount("!")
                .unsigned(true)
                .build());
    }

    @Override
    public RawTx createUnsignedTransactionSendingEntireBalance(String destinationAddress, TxoValue fee) {
        checkArgument(fee != null, "`fee` must not be null");

        return this.createTransaction(PaytoParams.builder()
                .destination(destinationAddress)
                .amount("!")
                .unsigned(true)
                .fee(BtcTxoValues.toBtc(fee).toPlainString())
                .build());
    }

    @Override
    public RawTx createUnsignedTransaction(TxoValue value, String destinationAddress, String changeAddress) {
        return this.createTransaction(PaytoParams.builder()
                .destination(destinationAddress)
                .amount(BtcTxoValues.toBtc(value).toPlainString())
                .unsigned(true)
                .changeAddress(changeAddress)
                .build());
    }

    @Override
    public RawTx createUnsignedTransaction(TxoValue value,
                                           String destinationAddress,
                                           String changeAddress,
                                           TxoValue fee) {
        checkArgument(fee != null, "`fee` must not be null");

        return this.createTransaction(PaytoParams.builder()
                .destination(destinationAddress)
                .amount(BtcTxoValues.toBtc(value).toPlainString())
                .unsigned(true)
                .changeAddress(changeAddress)
                .fee(BtcTxoValues.toBtc(fee).toPlainString())
                .build());
    }

    /**
     * Sign an unsigned transaction.
     *
     * <p>This method will throw an exception if the returned transaction from
     * electrum looks the same as the incoming unsigned transaction.
     * Electrum does not raise an error if the address is "watchonly",
     * but silently returns the unsigned transaction again.
     * WTF electrum!
     *
     * @return a signed transaction
     * @throws IllegalStateException if electrum did not change the transaction
     */
    @Override
    public RawTx signTransaction(SignTransactionParams params) {
        String signtransaction = delegate.signtransaction(
                params.getTx(),
                params.getPassword(),
                params.getWalletPath()
        );

        byte[] raw = fromHexOrBase64(signtransaction);

        String hex = HexFormat.of().formatHex(raw);
        boolean rawTxHasNotChanged = params.getTx().equals(hex);
        if (rawTxHasNotChanged) {
            throw new IllegalStateException("Transaction has not been signed by electrum - "
                                            + "maybe you have loaded a watchonly wallet?");
        }

        return SimpleRawTx.builder()
                .hex(hex)
                .signed(true)
                .build();
    }

    @Override
    public String broadcast(RawTx rawTx) {
        return this.delegate.broadcast(rawTx.getHex());
    }

    @Override
    public List<String> createMnemonicSeed() {
        return Arrays.asList(delegate.makeseed().split(" "));
    }

    @Override
    public Wallet createWallet(CreateParams params) {
        if (params.getEncryptFile() != null &&
            params.getPassword() == null) {
            throw new IllegalArgumentException("'password' must not be empty if encryption is enabled");
        }

        CreateResponse result = delegate.create(
                params.getPassphrase(),
                params.getEncryptFile(),
                params.getSeedType(),
                params.getPassword(),
                params.getWalletPath()
        );

        return new Wallet() {
            @Override
            public Seed getSeed() {
                return () -> Arrays.asList(result.getSeed().split(" "));
            }

            @Override
            public String getPath() {
                return result.getPath();
            }
        };
    }

    @Override
    public Boolean isWalletSynchronized() {
        return delegate.issynchronized();
    }

    @Override
    public Boolean isWalletSynchronized(IsSynchronizedParams params) {
        return delegate.issynchronized(params.getWalletPath());
    }

    @Override
    public Balance getBalance() {
        return SimpleBalance.from(delegate.getbalance());
    }

    @Override
    public Balance getBalance(GetBalanceParams params) {
        return SimpleBalance.from(delegate.getbalance(params.getWalletPath()));
    }

    /**
     * List wallets open in daemon
     *
     * @return A list of open wallets
     */
    @Override
    public List<ListWalletEntry> listOpenWallets() {
        return delegate.listwallets();
    }

    @Override
    public List<String> listAddresses() {
        return delegate.listaddresses();
    }

    @Override
    public List<String> listAddresses(ListAddressOptions options) {
        return delegate.listaddresses(
                options.getReceiving(),
                options.getChange(),
                options.getFrozen(),
                options.getUnused(),
                options.getFunded()
        );
    }

    @Override
    public List<String> listAddressesFunded() {
        return delegate.listaddresses(true);
    }

    @Override
    public List<String> listAddressesUnfunded() {
        return delegate.listaddresses(false);
    }

    @Override
    public List<AddressWithBalance> listAddressesWithBalance() {
        return listAddressesWithBalance(ListAddressOptions.all());
    }

    @Override
    public List<AddressWithBalance> listAddressesWithBalance(ListAddressOptions options) {
        List<List<String>> result = delegate.listaddresseswithbalance(
                options.getReceiving(),
                options.getChange(),
                options.getFrozen(),
                options.getUnused(),
                options.getFunded(),
                true,
                true
        );
        return result.stream()
                .filter(it -> it.size() >= 2)
                .map(SimpleAddressWithBalance::from)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void setLabel(SetLabelParams params) {
        delegate.setlabel(params.getKey(), params.getLabel(), params.getWalletPath());
    }

    @Override
    public Boolean isOwnerOfAddress(IsMineParams params) {
        return delegate.ismine(params.getAddress(), params.getWalletPath());
    }

    @Override
    public Optional<String> getUnusedAddress(GetUnusedAddressParams params) {
        return Optional.ofNullable(delegate.getunusedaddress(params.getWalletPath()));
    }

    @Override
    public String createNewAddress(CreateNewAddressParams params) {
        return delegate.createnewaddress(params.getWalletPath());
    }

    @Override
    public Balance getAddressBalance(String address) {
        AddressBalanceResponse result = delegate.getaddressbalance(address);
        return SimpleBalance.from(result);
    }

    @Override
    public Utxos getAddressUnspent(String address) {
        List<AddressUnspentResponse.Utxo> result = delegate.getaddressunspent(address);
        return SimpleUtxos.from(result);
    }

    @Override
    public List<TxHashAndBlockHeight> getAddressHistory(String address) {
        List<AddressHistoryResponse.Entry> getaddresshistory = delegate.getaddresshistory(address);
        return getaddresshistory.stream()
                .map(SimpleTxHashAndBlockHeight::from)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    @SneakyThrows
    public OnchainHistory getOnchainHistory() {
        List<OnchainHistoryResponse.HistoricTransaction> result = delegate.onchainhistory(true);
        return SimpleOnchainHistory.from(result);
    }

    @Override
    public OnchainSummary getOnchainCapitalGains() {
        OnchainCapitalGainsResponse result = delegate.onchaincapitalgains();
        return SimpleOnchainSummary.from(result);
    }

    @Override
    public RawTx getRawTransaction(String txHash) {
        String gettransaction = delegate.gettransaction(txHash);

        byte[] raw = fromHexOrBase64(gettransaction);

        return SimpleRawTx.builder()
                .hex(HexFormat.of().formatHex(raw))
                .signed(looksLikePsbt(gettransaction))
                .build();
    }

    @Override
    public Tx getDeserializedTransaction(String txHash) {
        RawTx result = this.getRawTransaction(txHash);
        return getDeserializedTransaction(result);
    }

    @Override
    public Tx getDeserializedTransaction(RawTx rawTx) {
        DeserializeResponse deserialize = delegate.deserialize(rawTx.getHex());

        List<DeserializeResponse.Input> inputs = deserialize.getInputs();
        List<DeserializeResponse.Output> outputs = deserialize.getOutputs();

        return SimpleTx.builder()
                .locktime(deserialize.getLockTime())
                .inputs(inputs.stream()
                        .map(it -> SimpleTx.SimpleTxInput.builder()
                                .txHash(it.getPrevoutHash())
                                .outputIndex(it.getPrevoutN())
                                .address(it.getAddress())
                                .value(Optional.ofNullable(it.getValue())
                                        .map(SimpleTxoValue::of)
                                        .orElse(null))
                                .sequenceNumber(it.getSequence())
                                .unlockingScript(it.getScriptSig())
                                .witness(it.getWitness())
                                .build())
                        .toList()
                )
                .outputs(outputs.stream()
                        .map(it -> SimpleTx.SimpleTxOutput.builder()
                                .value(SimpleTxoValue.of(it.getValue()))
                                .lockingScript(it.getScriptPubKey())
                                .address(it.getAddress())
                                .build())
                        .toList()
                )
                .build();
    }

    @Override
    public GetInfoResponse getInfo() {
        return delegate.getinfo();
    }

    @Override
    public Feerate getFeerate() {
        GetFeerateResponse result = delegate.getfeerate();
        return SimpleFeerate.builder()
                .policy(result.getPolicy())
                .satPerVbyte(SimpleSatPerVbyte.builder()
                        .satPerVbyte(BigDecimal.valueOf(result.getSatPerKvb())
                                .divide(BigDecimal.valueOf(1_000), 2, RoundingMode.UP))
                        .build())
                .build();
    }

    @Override
    public boolean changeGapLimit(ChangeGapLimitParams params) {
        return delegate.changegaplimit(
                params.getGaplimit(),
                true,
                params.getWalletPath()
        );
    }

    @Override
    public int getMinAcceptableGap(GetMinAcceptableGapParams params) {
        return delegate.getminacceptablegap(params.getWalletPath());
    }

    @Override
    public boolean loadWallet(LoadWalletParams params) {
        delegate.loadwallet(params.getWalletPath(), params.getPassword());
        return true;
    }

    @Override
    public boolean unlockWallet(UnlockWalletParams params) {
        delegate.unlock(params.getWalletPath(), params.getPassword());
        return true;
    }

    @Override
    public Boolean closeWallet(CloseWalletParams params) {
        return delegate.closewallet(params.getWalletPath());
    }

    @Override
    public RestoreResponse restoreWallet(RestoreParams params) {
        return delegate.restore(
                params.getText(),
                params.getPassphrase(),
                params.getEncryptFile(),
                params.getPassword(),
                params.getWalletPath()
        );
    }

    @Override
    public List<String> getMnemonicSeed(GetSeedParams params) {
        String getseed = delegate.getseed(params.getPassword(), params.getWalletPath());

        boolean seedIsAbsent = getseed == null || getseed.isEmpty();
        if (seedIsAbsent) {
            throw new IllegalStateException("Seed has not been returned by electrum - "
                                            + "maybe you have loaded a watchonly wallet?");
        }

        return splitMnemonicSeed(getseed);
    }

    @Override
    public Boolean addAddressChangedNotificationCallback(String address, String url) {
        return delegate.notify(address, url);
    }

    @Override
    public String encryptMessage(String publicKeyHex, String plaintext) {
        return delegate.encrypt(publicKeyHex, plaintext);
    }

    @Override
    public String decryptMessage(DecryptParams params) {
        return delegate.decrypt(params.getPublicKey(),
                params.getEncryptedMessage(),
                params.getPassword(),
                params.getWalletPath());
    }

    @Override
    public String signMessage(String address, String message, @Nullable String walletPassphrase) {
        return delegate.signmessage(address, message, walletPassphrase);
    }

    @Override
    public Boolean verifyMessage(String address, String signature, String message) {
        return delegate.verifymessage(address, signature, message);
    }

    @Override
    public List<String> getPublicKeys(String address) {
        return delegate.getpubkeys(address);
    }

    @Override
    public Version daemonVersion() {
        return SimpleVersion.from(delegate.version());
    }

    @Override
    public Map<String, String> daemonVersionInfo() {
        return delegate.versioninfo();
    }

    @Override
    public Boolean isValidAddress(String address) {
        return delegate.validateaddress(address);
    }

    @Override
    public Future<?> waitForWalletSynchronization() {
        return syncExecutor.submit(delegate::waitforsync);
    }

    private static byte[] fromHexOrBase64(String value) {
        if (looksLikePsbt(value)) {
            return Base64.getDecoder().decode(value);
        }
        try {
            return HexFormat.of().parseHex(value);
        } catch (Exception e) {
            return Base64.getDecoder().decode(value);
        }
    }

    @Override
    public void close() {
        boolean executorShutdownSuccessful = shutdownAndAwaitTermination(syncExecutor, Duration.ofSeconds(10));
        if (!executorShutdownSuccessful) {
            log.warn("unclean shutdown of executor service");
        }
    }
}
