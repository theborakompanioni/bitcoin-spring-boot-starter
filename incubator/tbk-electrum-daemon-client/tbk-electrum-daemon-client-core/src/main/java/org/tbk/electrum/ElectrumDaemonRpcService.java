package org.tbk.electrum;

import com.github.arteam.simplejsonrpc.client.JsonRpcId;
import com.github.arteam.simplejsonrpc.client.JsonRpcParams;
import com.github.arteam.simplejsonrpc.client.ParamsType;
import com.github.arteam.simplejsonrpc.client.generator.AtomicLongIdGenerator;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcOptional;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcParam;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcService;
import org.tbk.electrum.command.*;
import org.tbk.electrum.model.History;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a raw electrum rpc interfaces.
 * Uses the same name for the method as electrum does.
 * e.g. method for command "makeseed" is named {@link #makeseed()}
 *
 * <p>This interface is tested with electrum v3.3.8.
 *
 * <p>Not every parameter is currently implemented.
 *
 * <p>Following methods are still missing to be feature-complete:
 * [ ] "addrequest",
 * [ ] "addtransaction",
 * [ ] "clearrequests",
 * [ ] "commands",
 * [ ] "create",
 * [ ] "createmultisig",
 * [ ] "freeze",
 * [ ] "getalias",
 * [ ] "getfeerate",
 * [ ] "getmasterprivate",
 * [ ] "getmerkle",
 * [ ] "getmpk",
 * [ ] "getprivatekeys",
 * [ ] "getrequest",
 * [ ] "getservers",
 * [ ] "help",
 * [ ] "importprivkey",
 * [ ] "listcontacts",
 * [ ] "listrequests",
 * [ ] "listunspent",
 * [ ] "password",
 * [ ] "paytomany",
 * [ ] "restore",
 * [ ] "rmrequest",
 * [ ] "searchcontacts",
 * [ ] "serialize",
 * [ ] "setlabel",
 * [ ] "signrequest",
 * [ ] "sweep",
 * [ ] "unfreeze",
 * [ ] "validateaddress",
 */
@JsonRpcService
@JsonRpcId(AtomicLongIdGenerator.class)
@JsonRpcParams(ParamsType.MAP)
public interface ElectrumDaemonRpcService {

    /**
     * Returns the version of Electrum that is currently running.
     *
     * @return the version of Electrum that is currently running
     */
    @JsonRpcMethod("version")
    String version();

    /**
     * Generates a new seed. Does not change the current seed.
     *
     * @return a new seed
     */
    @JsonRpcMethod("make_seed")
    String makeseed();

    /**
     * Return the wallet’s mnemonic seed.
     *
     * @return the wallet's mnemonic seed
     */
    @JsonRpcMethod("getseed")
    String getseed(@JsonRpcOptional @JsonRpcParam("password") String password);

    /**
     * return wallet synchronization status
     *
     * @return wallet synchronization status
     */
    @JsonRpcMethod("is_synchronized")
    Boolean issynchronized();

    @JsonRpcMethod("getbalance")
    BalanceResponse getbalance();

    /**
     * Returns true if the address belongs to the wallet, or false otherwise.
     *
     * @param address the wallet address
     * @return true if the address belongs to the wallet, or false otherwise
     */
    @JsonRpcMethod("ismine")
    Boolean ismine(@JsonRpcParam("address") String address);

    /**
     * Returns a list of addresses controlled by the wallet.
     *
     * @return a list of addresses controlled by the wallet
     */
    @JsonRpcMethod("listaddresses")
    List<String> listaddresses();

    @JsonRpcMethod("listaddresses")
    List<String> listaddresses(@JsonRpcParam("funded") boolean funded);

    @JsonRpcMethod("listaddresses")
    List<String> listaddresses(@JsonRpcOptional @JsonRpcParam("receiving") Boolean receiving,
                               @JsonRpcOptional @JsonRpcParam("change") Boolean change,
                               @JsonRpcOptional @JsonRpcParam("labels") Boolean labels,
                               @JsonRpcOptional @JsonRpcParam("frozen") Boolean frozen,
                               @JsonRpcOptional @JsonRpcParam("unused") Boolean unused,
                               @JsonRpcOptional @JsonRpcParam("funded") Boolean funded,
                               @JsonRpcOptional @JsonRpcParam("balance") Boolean balance);

    /**
     * Returns the first unused address of the wallet, or none if all addresses are used.
     * An address is considered to be used if it has received a transaction, or if it is used in a payment request.
     *
     * @return the first unused address of the wallet, or none if all addresses are used.
     */
    @JsonRpcMethod("getunusedaddress")
    @Nullable
    String getunusedaddress();

    /**
     * Creates a new receiving address.
     *
     * @return a new receiving address
     */
    @JsonRpcMethod("createnewaddress")
    String createnewaddress();

    /**
     * Electrum actually returns a json embedded in a string -_-
     * Represented in {@link History} but you must parse it yourself.
     *
     * @return a string containing the HistoryResponse json
     * @deprecated use {@link #onchainhistory} instead
     */
    @Deprecated
    @JsonRpcMethod("history")
    String history();

    /**
     * Electrum actually returns a json embedded in a string -_-
     * Represented in {@link History} but you must parse it yourself.
     *
     * @return a string containing the HistoryResponse json
     * @deprecated use {@link #onchainhistory} instead
     */
    @Deprecated
    @JsonRpcMethod("history")
    String history(@JsonRpcParam("show_addresses") boolean showAddresses);

    @JsonRpcMethod("onchain_history")
    HistoryResponse onchainhistory();

    @JsonRpcMethod("onchain_history")
    HistoryResponse onchainhistory(@JsonRpcParam("show_addresses") boolean showAddresses);

    @JsonRpcMethod("onchain_history")
    HistoryResponse onchainhistory(@JsonRpcParam("show_addresses") boolean showAddresses,
                                   @JsonRpcParam("from_height") long fromHeight);

    @JsonRpcMethod("onchain_history")
    HistoryResponse onchainhistory(@JsonRpcParam("show_addresses") boolean showAddresses,
                                   @JsonRpcParam("from_height") long fromHeight,
                                   @JsonRpcParam("to_height") long toHeight);

    @JsonRpcMethod("list_wallets")
    List<ListWalletEntry> listwallets();

    /**
     * Returns the balance of address. This is a walletless server query. Results are not checked by SPV.
     *
     * @param address the wallet address
     * @return the balance of address
     */
    @JsonRpcMethod("getaddressbalance")
    AddressBalanceResponse getaddressbalance(@JsonRpcParam("address") String address);

    /**
     * Get the address history.
     * e.g.:
     * [
     * {
     * "height": 1297755,
     * "tx_hash": "49cc9631145613d434fcd729477664db90e233ada72d962c7f39b55bcbf4b608"
     * },
     * {
     * "height": 1297757,
     * "tx_hash": "ed1350d20674d9a9596d66be869bba2da135ac24111a11205c0255221b6e95b6"
     * }
     * ]
     *
     * @param address the wallet address
     * @return the address history
     */
    @JsonRpcMethod("getaddresshistory")
    List<AddressHistoryResponse.Entry> getaddresshistory(@JsonRpcParam("address") String address);

    /**
     * Returns a list of unspent transaction outputs for the given address.
     *
     * @param address the wallet address
     * @return a list of unspent transaction outputs
     */
    @JsonRpcMethod("getaddressunspent")
    List<AddressUnspentResponse.Utxo> getaddressunspent(@JsonRpcParam("address") String address);

    @JsonRpcMethod("gettransaction")
    String gettransaction(@JsonRpcParam("txid") String txId);

    @JsonRpcMethod("deserialize")
    DeserializeResponse deserialize(@JsonRpcParam("tx") String tx);

    /**
     * @deprecated Use {@link #getinfo} instead.
     */
    @Deprecated
    @JsonRpcMethod("getinfo")
    GetInfoResponse status(@JsonRpcParam("config_options") DaemonStatusRequest request);

    @JsonRpcMethod("getinfo")
    GetInfoResponse getinfo();

    @JsonRpcMethod("load_wallet")
    Boolean loadwallet(@JsonRpcParam("config_options") DaemonLoadWalletRequest request);

    @JsonRpcMethod("close_wallet")
    Boolean closewallet(@JsonRpcParam("config_options") DaemonCloseWalletRequest request);

    @JsonRpcMethod("getconfig")
    @Nullable
    Object getconfig(@JsonRpcParam("key") String key);

    /**
     * Set a configuration variable. 'value' may be a string or a Python expression.
     *
     * @param key   the config key
     * @param value the config value
     * @return will always return true or errors (in v3.3.8)
     */
    @JsonRpcMethod("setconfig")
    Boolean setconfig(@JsonRpcParam("key") String key, @JsonRpcParam("value") String value);


    @JsonRpcMethod("payto")
    String payto(@JsonRpcParam("destination") String destination,
                                 @JsonRpcParam("amount") String amount,
                                 @JsonRpcOptional @JsonRpcParam("change_addr") String changeAddr,
                                 @JsonRpcOptional @JsonRpcParam("unsigned") Boolean unsigned,
                                 @JsonRpcParam("password") String password);

    @JsonRpcMethod("payto")
    String payto(
            @JsonRpcParam("destination") String destination,
            @JsonRpcParam("amount") String amount,
            @JsonRpcOptional @JsonRpcParam("fee") String fee,
            @JsonRpcOptional @JsonRpcParam("feerate") Object feerate, // untested atm
            @JsonRpcOptional @JsonRpcParam("from_addr") String fromAddr, // untested atm
            @JsonRpcOptional @JsonRpcParam("from_coins") Object fromCoins, // untested atm
            @JsonRpcOptional @JsonRpcParam("change_addr") String changeAddr,
            @JsonRpcOptional @JsonRpcParam("nocheck") Boolean nocheck, // untested atm
            @JsonRpcOptional @JsonRpcParam("unsigned") Boolean unsigned,
            @JsonRpcOptional @JsonRpcParam("addtransaction") Object addtransaction, // untested atm
            @JsonRpcOptional @JsonRpcParam("rbf") Boolean rbf,
            @JsonRpcOptional @JsonRpcParam("password") String password,
            @JsonRpcOptional @JsonRpcParam("locktime") Long locktime // untested atm
    );

    /**
     * Broadcast a transaction to the network.
     *
     * @param tx a raw transaction (hexadecimal)
     * @return tx hash
     */
    @JsonRpcMethod("broadcast")
    String broadcast(@JsonRpcParam("tx") String tx);

    /**
     * password is optional (if you have an unencrypted wallet - which is highly discouraged).
     *
     * @param tx       a raw transaction (hexadecimal)
     * @param password the wallet passphrase (null if unencrypted).
     * @return a signed transaction
     */
    @JsonRpcMethod("signtransaction")
    String signtransaction(@JsonRpcParam("tx") String tx,
                                           @JsonRpcOptional @JsonRpcParam("password") String password);

    /**
     * Watch an address. Everytime the address changes, a http POST is sent to the URL.
     *
     * @param address the address being watched
     * @param url     the url to POST to when address balances changes
     * @return will always return true or errors (in v3.3.8)
     */
    @JsonRpcMethod("notify")
    Boolean notify(@JsonRpcParam("address") String address, @JsonRpcParam("URL") String url);

    /**
     * Encrypt a message with a public key. Use quotes if the message contains whitespaces.
     *
     * @param pubkey  the pubkey
     * @param message the plaintext message
     * @return an encrypted message
     */
    @JsonRpcMethod("encrypt")
    String encrypt(@JsonRpcParam("pubkey") String pubkey, @JsonRpcParam("message") String message);

    /**
     * Decrypt a message encrypted with a public key.
     *
     * @param pubkey    the pubkey
     * @param encrypted the encrypted message
     * @param password  the wallet password
     * @return a decrypted message
     */
    @JsonRpcMethod("decrypt")
    String decrypt(@JsonRpcParam("pubkey") String pubkey,
                   @JsonRpcParam("encrypted") String encrypted,
                   @JsonRpcOptional @JsonRpcParam("password") String password);

    /**
     * Return the public keys for a wallet address.
     *
     * @param address the wallet address
     * @return the public keys for a wallet address
     */
    @JsonRpcMethod("getpubkeys")
    List<String> getpubkeys(@JsonRpcParam("address") String address);

    /**
     * Sign a message with a key. Use quotes if your message contains whitespaces
     *
     * @param address  the wallet address
     * @param message  the message
     * @param password the wallet password
     * @return signature base64
     */
    @JsonRpcMethod("signmessage")
    String signmessage(@JsonRpcParam("address") String address,
                       @JsonRpcParam("message") String message,
                       @JsonRpcOptional @JsonRpcParam("password") String password);

    /**
     * Verify a signature.
     *
     * @param address   the wallet address
     * @param signature signature in base64
     * @param message   the message
     * @return true if the signature is valid
     */
    @JsonRpcMethod("verifymessage")
    Boolean verifymessage(@JsonRpcParam("address") String address,
                          @JsonRpcParam("signature") String signature,
                          @JsonRpcParam("message") String message);
}
