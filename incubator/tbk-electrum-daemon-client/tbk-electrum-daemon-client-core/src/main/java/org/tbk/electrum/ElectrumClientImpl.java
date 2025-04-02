package org.tbk.electrum;

import lombok.SneakyThrows;
import org.tbk.electrum.command.*;
import org.tbk.electrum.model.*;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class ElectrumClientImpl implements ElectrumClient {

    private static List<String> splitMnemonicSeed(String seed) {
        return Arrays.asList(seed.split(" "));
    }

    private final ElectrumDaemonRpcService delegate;

    public ElectrumClientImpl(ElectrumDaemonRpcService delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public Optional<Object> daemonGetConfig(ConfigKey key) {
        return Optional.ofNullable(delegate.getconfig(key.name()));
    }

    @Override
    public void daemonSetConfig(ConfigKey key, String value) {
        delegate.setconfig(key.name(), value);
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
                    params.getWalletPath(),
                    params.getForgetconfig()
            );

            // payto can be base64 or hex
            // hex: for finalized tx?
            // base64: for unsigned tx?
            byte[] raw = fromHexOrBase64(payto);

            return SimpleRawTx.builder()
                    .hex(HexFormat.of().formatHex(raw))
                    .finalized(Boolean.TRUE.equals(params.getUnsigned()))
                    .complete(Boolean.TRUE.equals(params.getUnsigned()))
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
    public RawTx createUnsignedTransaction(TxoValue value, String destinationAddress, String changeAddress, TxoValue fee) {
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
                params.getWalletPath(),
                params.getForgetconfig(),
                params.getIknowwhatimdoing()
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
                .finalized(true)
                .complete(true)
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
    public Boolean isWalletSynchronized() {
        return delegate.issynchronized();
    }

    @Override
    public Boolean isWalletSynchronized(IsSynchronizedParams params) {
        return delegate.issynchronized(params.getWalletPath(), params.getForgetconfig());
    }

    @Override
    public Balance getBalance() {
        return SimpleBalance.from(delegate.getbalance());
    }

    @Override
    public Balance getBalance(GetBalanceParams params) {
        return SimpleBalance.from(delegate.getbalance(
                params.getWalletPath(),
                params.getForgetconfig()
        ));
    }

    /**
     * List wallets open in daemon
     *
     * @return
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
                options.getLabels(),
                options.getFrozen(),
                options.getUnused(),
                options.getFunded(),
                options.getBalance());
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
    public Boolean isOwnerOfAddress(IsMineParams params) {
        return delegate.ismine(params.getAddress(),
                params.getWalletPath(),
                params.getForgetconfig());
    }

    @Override
    public Optional<String> getUnusedAddress(GetUnusedAddressParams params) {
        return Optional.ofNullable(delegate.getunusedaddress(
                params.getWalletPath(),
                params.getForgetconfig()
        ));
    }

    @Override
    public String createNewAddress() {
        return delegate.createnewaddress();
    }

    @Override
    public Balance getAddressBalance(String address) {
        AddressBalanceResponse addressBalance = delegate.getaddressbalance(address);

        return SimpleBalance.builder()
                .confirmed(BtcTxoValues.fromBtcString(addressBalance.getConfirmed()))
                .unconfirmed(BtcTxoValues.fromBtcString(addressBalance.getUnconfirmed()))
                .build();
    }

    @Override
    public Utxos getAddressUnspent(String address) {
        List<Utxo> utxos = delegate.getaddressunspent(address).stream()
                .map(val -> SimpleUtxo.builder()
                        .height(val.getHeight())
                        .txHash(val.getTxHash())
                        .txPos(val.getTxPos())
                        .value(SimpleTxoValue.of(val.getValue()))
                        .build())
                .collect(Collectors.toUnmodifiableList());

        return SimpleUtxos.builder()
                .utxos(utxos)
                .build();
    }

    @Override
    public List<TxHashAndBlockHeight> getAddressHistory(String address) {
        return delegate.getaddresshistory(address).stream()
                .map(it -> SimpleTxHashAndBlockHeight.builder()
                        .height(it.getHeight())
                        .txHash(it.getTxHash())
                        .build())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    @SneakyThrows
    public OnchainHistory getOnchainHistory() {
        HistoryResponse history = delegate.onchainhistory(true);

        HistoryResponse.Summary summary = history.getSummary();
        List<HistoryResponse.Transaction> transactions = history.getTransactions();

        SimpleOnchainHistory.SimpleSummary historySummary = SimpleOnchainHistory.SimpleSummary.builder()
                .startBalance(BtcTxoValues.fromBtcStringOrZero(Optional.ofNullable(summary.getBegin()).map(HistoryResponse.Summary.SummaryTime::getBalance).orElse(null)))
                .endBalance(BtcTxoValues.fromBtcStringOrZero(Optional.ofNullable(summary.getEnd()).map(HistoryResponse.Summary.SummaryTime::getBalance).orElse(null)))
                .incoming(BtcTxoValues.fromBtcStringOrZero(Optional.ofNullable(summary.getFlow()).map(HistoryResponse.Summary.SummaryFlow::getIncoming).orElse(null)))
                .outgoing(BtcTxoValues.fromBtcStringOrZero(Optional.ofNullable(summary.getFlow()).map(HistoryResponse.Summary.SummaryFlow::getOutgoing).orElse(null)))
                .build();

        return SimpleOnchainHistory.builder()
                .summary(historySummary)
                .transactions(transactions.stream()
                        .map(it -> {
                            List<SimpleOnchainHistory.SimpleHistoryTxInput> inputsOrEmpty = Optional.ofNullable(it.getInputs())
                                    .map(inputs -> inputs.stream()
                                            .map(input -> SimpleOnchainHistory.SimpleHistoryTxInput.builder()
                                                    .txHash(input.getPrevoutHash())
                                                    .outputIndex(input.getPrevoutN())
                                                    .build())
                                            .toList())
                                    .orElseGet(Collections::emptyList);

                            List<SimpleOnchainHistory.SimpleHistoryTxOutput> outputsOrEmpty = Optional.ofNullable(it.getOutputs())
                                    .map(outputs -> outputs.stream()
                                            .map(output -> SimpleOnchainHistory.SimpleHistoryTxOutput.builder()
                                                    .value(BtcTxoValues.fromBtcString(output.getValue()))
                                                    .address(output.getAddress())
                                                    .build())
                                            .toList())
                                    .orElseGet(Collections::emptyList);

                            Instant timestampOrNull = Optional.ofNullable(it.getTimestamp())
                                    .map(Instant::ofEpochSecond)
                                    .orElse(null);

                            return SimpleOnchainHistory.SimpleTransaction.builder()
                                    .balance(BtcTxoValues.fromBtcString(it.getBalance()))
                                    .txHash(it.getTxId())
                                    .value(BtcTxoValues.fromBtcString(it.getValue()))
                                    .incoming(it.isIncoming())
                                    .confirmations(it.getConfirmations())
                                    .timestamp(timestampOrNull)
                                    .height(it.getHeight())
                                    .label(it.getLabel())
                                    .txPosInBlock(it.getTxPosInBlock())
                                    .inputs(inputsOrEmpty)
                                    .outputs(outputsOrEmpty)
                                    .build();
                        })
                        .toList())
                .build();
    }

    @Override
    public RawTx getRawTransaction(String txHash) {
        String gettransaction = delegate.gettransaction(txHash);

        byte[] raw = fromHexOrBase64(gettransaction);

        return SimpleRawTx.builder()
                .hex(HexFormat.of().formatHex(raw))
                .finalized(true)
                .complete(true)
                .build();
    }

    @Override
    public Tx getDeserializedTransaction(String txHash) {
        RawTx rawTx = this.getRawTransaction(txHash);
        return getDeserializedTransaction(rawTx);
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
    public boolean loadWallet(LoadWalletParams params) {
        delegate.loadwallet(params.getWalletPath(),
                params.getPassword(),
                params.getUnlock(),
                params.getForgetconfig()
        );
        return true;
    }

    @Override
    public Boolean closeWallet(CloseWalletParams params) {
        return delegate.closewallet(params.getWalletPath(), params.getForgetconfig());
    }

    @Override
    public List<String> getMnemonicSeed(GetSeedParams params) {
        String getseed = delegate.getseed(params.getPassword(),
                params.getWalletPath(),
                params.getForgetconfig());

        boolean seedIsAbsent = getseed == null || getseed.isEmpty();
        if (seedIsAbsent) {
            throw new IllegalStateException("Seed has not been returned by electrum - "
                                            + "maybe you have loaded a watchonly wallet?");
        }

        return splitMnemonicSeed(getseed);
    }

    @Override
    public Boolean addAddressChangedNotificationCallback(String address, String url) {
        return this.delegate.notify(address, url);
    }

    @Override
    public String encryptMessage(String publicKeyHex, String plaintext) {
        return this.delegate.encrypt(publicKeyHex, plaintext);
    }

    @Override
    public String decryptMessage(String publicKeyHex, String encryptedMessage, @Nullable String walletPassphrase) {
        return this.delegate.decrypt(publicKeyHex, encryptedMessage, walletPassphrase);
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
        return this.delegate.getpubkeys(address);
    }

    @Override
    public Version daemonVersion() {
        return SimpleVersion.from(delegate.version());
    }

    @Override
    public Map<String, String> daemonVersionInfo() {
        return delegate.versioninfo();
    }

    private static byte[] fromHexOrBase64(String value) {
        try {
            return HexFormat.of().parseHex(value);
        } catch (Exception e) {
            return Base64.getDecoder().decode(value);
        }
    }
}
