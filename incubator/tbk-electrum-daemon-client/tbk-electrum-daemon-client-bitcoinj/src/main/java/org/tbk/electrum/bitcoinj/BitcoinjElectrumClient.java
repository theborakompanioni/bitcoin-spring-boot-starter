package org.tbk.electrum.bitcoinj;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.bitcoinj.model.BitcoinjBalance;
import org.tbk.electrum.bitcoinj.model.BitcoinjUtxos;

import java.util.List;
import java.util.Optional;

public interface BitcoinjElectrumClient {

    ElectrumClient delegate();

    BitcoinjBalance getBalance();

    List<Address> listAddresses();

    List<Address> listAddresses(ElectrumClient.ListAddressOptions options);

    List<Address> listAddressesFunded();

    List<Address> listAddressesUnfunded();

    Boolean isOwnerOfAddress(Address address);

    Optional<Address> getUnusedAddress();

    Address createNewAddress();

    BitcoinjBalance getAddressBalance(Address address);

    BitcoinjUtxos getAddressUnspent(Address address);

    Transaction getTransaction(Sha256Hash txHash);
}
