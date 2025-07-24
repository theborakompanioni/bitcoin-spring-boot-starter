package org.tbk.electrum.bitcoinj;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.bitcoinj.model.BitcoinjBalance;
import org.tbk.electrum.bitcoinj.model.BitcoinjUtxos;
import org.tbk.electrum.common.ListAddressParams;
import org.tbk.electrum.rpc.command.GetBalanceParams;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface BitcoinjElectrumClient {

    ElectrumClient delegate();

    default BitcoinjBalance getBalance() {
        return getBalance(GetBalanceParams.builder().build());
    }

    BitcoinjBalance getBalance(GetBalanceParams params);

    List<ECKey> getPublicKeys(Address address);

    default List<Address> listAddresses() {
        return listAddresses(ListAddressParams.all());
    }

    List<Address> listAddresses(ListAddressParams options);

    List<Address> listAddressesFunded();

    Boolean isOwnerOfAddress(Address address);

    Optional<Address> getUnusedAddress();

    Address createNewAddress();

    BitcoinjBalance getAddressBalance(Address address);

    BitcoinjUtxos getAddressUnspent(Address address);

    Transaction getTransaction(Sha256Hash txHash);

    Boolean addAddressChangedNotificationCallback(Address address, URI url);
}
