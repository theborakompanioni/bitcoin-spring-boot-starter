package org.tbk.electrum.bitcoinj;

import org.bitcoinj.base.Address;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.bitcoinj.common.GetPubkeysParams;
import org.tbk.electrum.bitcoinj.common.GetTransactionParams;
import org.tbk.electrum.bitcoinj.common.IsMineParams;
import org.tbk.electrum.bitcoinj.model.BitcoinjBalance;
import org.tbk.electrum.bitcoinj.model.BitcoinjUtxos;
import org.tbk.electrum.common.ListAddressParams;
import org.tbk.electrum.rpc.command.CreateNewAddressParams;
import org.tbk.electrum.rpc.command.GetBalanceParams;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface BitcoinjElectrumClient {

    ElectrumClient delegate();

    BitcoinjBalance getBalance(GetBalanceParams params);

    List<ECKey> getPublicKeys(GetPubkeysParams params);

    List<Address> listAddresses(ListAddressParams options);

    List<Address> listAddressesFunded();

    Boolean isOwnerOfAddress(IsMineParams params);

    Optional<Address> getUnusedAddress();

    Address createNewAddress(CreateNewAddressParams params);

    BitcoinjBalance getAddressBalance(Address address);

    BitcoinjUtxos getUtxosByAddress(Address address);

    Transaction getTransaction(GetTransactionParams params);

    Boolean addAddressChangedNotificationCallback(Address address, URI url);
}
