package org.tbk.electrum.bitcoinj;

import org.bitcoinj.core.*;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.bitcoinj.common.GetPubkeysParams;
import org.tbk.electrum.bitcoinj.common.IsMineParams;
import org.tbk.electrum.bitcoinj.model.BitcoinjBalance;
import org.tbk.electrum.bitcoinj.model.BitcoinjUtxos;
import org.tbk.electrum.common.ListAddressParams;
import org.tbk.electrum.model.Balance;
import org.tbk.electrum.model.RawTx;
import org.tbk.electrum.model.Utxos;
import org.tbk.electrum.rpc.command.CreateNewAddressParams;
import org.tbk.electrum.rpc.command.GetBalanceParams;

import java.net.URI;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.tbk.electrum.bitcoinj.BitcoinjHelper.toBitcoinjBalance;
import static org.tbk.electrum.bitcoinj.BitcoinjHelper.toBitcoinjUtxos;

public class BitcoinjElectrumClientImpl implements BitcoinjElectrumClient {

    private final NetworkParameters network;
    private final ElectrumClient delegate;

    public BitcoinjElectrumClientImpl(NetworkParameters network, ElectrumClient delegate) {
        this.network = requireNonNull(network);
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public ElectrumClient delegate() {
        return delegate;
    }

    @Override
    public BitcoinjBalance getBalance(GetBalanceParams params) {
        Balance balance = delegate.getBalance(params);
        return toBitcoinjBalance(balance);
    }

    @Override
    public List<ECKey> getPublicKeys(GetPubkeysParams params) {
        return delegate.getPublicKeys(org.tbk.electrum.rpc.command.GetPubkeysParams.builder()
                        .address(params.getAddress().toString())
                        .build()).stream()
                .map(it -> ECKey.fromPublicOnly(HexFormat.of().parseHex(it)))
                .toList();
    }

    @Override
    public List<Address> listAddresses(ListAddressParams options) {
        return this.delegate.listAddresses(options).stream()
                .map(it -> Address.fromString(this.network, it))
                .toList();
    }

    @Override
    public List<Address> listAddressesFunded() {
        return this.delegate.listAddressesFunded().stream()
                .map(it -> Address.fromString(this.network, it))
                .toList();
    }

    @Override
    public Boolean isOwnerOfAddress(IsMineParams params) {
        return this.delegate.isOwnerOfAddress(org.tbk.electrum.rpc.command.IsMineParams.builder()
                .address(params.getAddress().toString())
                .walletPath(params.getWalletPath())
                .build());
    }

    @Override
    public Optional<Address> getUnusedAddress() {
        return this.delegate.getUnusedAddress()
                .map(it -> Address.fromString(this.network, it));
    }

    @Override
    public Address createNewAddress(CreateNewAddressParams params) {
        return Address.fromString(this.network, this.delegate.createNewAddress(params));
    }

    @Override
    public BitcoinjBalance getAddressBalance(Address address) {
        Balance balance = delegate.getAddressBalance(address.toString());
        return toBitcoinjBalance(balance);
    }

    @Override
    public BitcoinjUtxos getUtxosByAddress(Address address) {
        Utxos addressUnspent = this.delegate.getUtxosByAddress(address.toString());
        return toBitcoinjUtxos(addressUnspent);
    }

    @Override
    public Transaction getTransaction(Sha256Hash txHash) {
        RawTx rawTransaction = this.delegate.getRawTransaction(txHash.toString());
        byte[] raw = HexFormat.of().parseHex(rawTransaction.getHex().toLowerCase());
        return new Transaction(this.network, raw);
    }

    @Override
    public Boolean addAddressChangedNotificationCallback(Address address, URI url) {
        return delegate.addAddressChangedNotificationCallback(address.toString(), url);
    }
}
