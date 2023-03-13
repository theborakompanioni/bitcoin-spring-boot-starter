package org.tbk.electrum.bitcoinj;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.bitcoinj.model.BitcoinjBalance;
import org.tbk.electrum.bitcoinj.model.BitcoinjUtxos;
import org.tbk.electrum.model.Balance;
import org.tbk.electrum.model.RawTx;
import org.tbk.electrum.model.Utxos;

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
    public BitcoinjBalance getBalance() {
        Balance balance = delegate.getBalance();

        return toBitcoinjBalance(balance);
    }

    @Override
    public List<Address> listAddresses() {
        List<String> addresses = this.delegate.listAddresses();

        return addresses.stream()
                .map(it -> Address.fromString(this.network, it))
                .toList();
    }

    @Override
    public List<Address> listAddresses(ElectrumClient.ListAddressOptions options) {
        List<String> addresses = this.delegate.listAddresses(options);

        return addresses.stream()
                .map(it -> Address.fromString(this.network, it))
                .toList();
    }

    @Override
    public List<Address> listAddressesFunded() {
        List<String> addresses = this.delegate.listAddressesFunded();

        return addresses.stream()
                .map(it -> Address.fromString(this.network, it))
                .toList();
    }

    @Override
    public List<Address> listAddressesUnfunded() {
        List<String> addresses = this.delegate.listAddressesUnfunded();

        return addresses.stream()
                .map(it -> Address.fromString(this.network, it))
                .toList();
    }

    @Override
    public Boolean isOwnerOfAddress(Address address) {
        return this.delegate.isOwnerOfAddress(address.toString());
    }

    @Override
    public Optional<Address> getUnusedAddress() {
        return this.delegate.getUnusedAddress()
                .map(it -> Address.fromString(this.network, it));
    }

    @Override
    public Address createNewAddress() {
        return Address.fromString(this.network, this.delegate.createNewAddress());
    }

    @Override
    public BitcoinjBalance getAddressBalance(Address address) {
        Balance balance = delegate.getAddressBalance(address.toString());

        return toBitcoinjBalance(balance);
    }

    @Override
    public BitcoinjUtxos getAddressUnspent(Address address) {
        Utxos addressUnspent = this.delegate.getAddressUnspent(address.toString());
        return toBitcoinjUtxos(addressUnspent);
    }

    @Override
    public Transaction getTransaction(Sha256Hash txHash) {
        RawTx rawTransaction = this.delegate.getRawTransaction(txHash.toString());
        byte[] raw = HexFormat.of().parseHex(rawTransaction.getHex().toLowerCase());
        return new Transaction(this.network, raw);
    }
}
