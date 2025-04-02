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

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Represents a raw electrum rpc interfaces.
 * Uses the same name for the method as electrum does.
 * e.g. method for command "makeseed" is named {@link #makeseed()}
 *
 * <p>This interface is tested with electrum v3.3.8.
 *
 * <p>Not every method is currently implemented.
 * <p>`electrum --regtest help`:
 * [
 * "add_peer",
 * "add_request",
 * "addtransaction",
 * "broadcast",
 * "bumpfee",
 * "changegaplimit",
 * "clear_invoices",
 * "clear_ln_blacklist",
 * "clear_requests",
 * "close_channel",
 * [x] "close_wallet",
 * "commands",
 * "convert_currency",
 * "convert_xkey",
 * [x] "create",
 * "createmultisig",
 * "createnewaddress",
 * "decode_invoice",
 * "decrypt",
 * "delete_invoice",
 * "delete_request",
 * "deserialize",
 * "dumpprivkeys",
 * "enable_htlc_settle",
 * "encrypt",
 * "export_channel_backup",
 * "freeze",
 * "freeze_utxo",
 * "get",
 * "get_channel_ctx",
 * "get_invoice",
 * "get_request",
 * "get_tx_status",
 * "get_watchtower_ctn",
 * "getaddressbalance",
 * "getaddresshistory",
 * "getaddressunspent",
 * "getalias",
 * "getbalance",
 * "getconfig",
 * "getfeerate",
 * "getinfo",
 * "getmasterprivate",
 * "getmerkle",
 * "getminacceptablegap",
 * "getmpk",
 * "getprivatekeyforpath",
 * "getprivatekeys",
 * "getpubkeys",
 * "getseed",
 * "getservers",
 * "gettransaction",
 * "getunusedaddress",
 * "help",
 * "import_channel_backup",
 * "importprivkey",
 * "is_synchronized",
 * "ismine",
 * "lightning_history",
 * "list_channels",
 * "list_invoices",
 * "list_peers",
 * "list_requests",
 * "list_wallets",
 * "listaddresses",
 * "listcontacts",
 * "listunspent",
 * "lnpay",
 * "load_wallet",
 * "make_seed",
 * "nodeid",
 * "normal_swap",
 * "notify",
 * "onchain_history",
 * "open_channel",
 * "password",
 * "payto",
 * "paytomany",
 * "rebalance_channels",
 * "removelocaltx",
 * "request_force_close",
 * "reset_liquidity_hints",
 * "restore",
 * "reverse_swap",
 * "searchcontacts",
 * "serialize",
 * "setconfig",
 * "setfeerate",
 * "setlabel",
 * "signmessage",
 * "signtransaction",
 * "signtransaction_with_privkey",
 * [x] "stop",
 * "sweep",
 * "unfreeze",
 * "unfreeze_utxo",
 * "validateaddress",
 * [x] "verifymessage",
 * [x] "version",
 * [x] "version_info"
 * ]
 */
@JsonRpcService
@JsonRpcId(AtomicLongIdGenerator.class)
@JsonRpcParams(ParamsType.MAP)
public interface ElectrumDaemonRpcService {

    /**
     * Generates a new seed. Does not change the current seed.
     *
     * @return a new seed
     */
    @JsonRpcMethod("make_seed")
    String makeseed();

    /**
     * Generates a new seed. Does not change the current seed.
     *
     * @param seed_type the type of seed to create, e.g. 'standard' or 'segwit'
     * @return a new seed
     */
    @JsonRpcMethod("make_seed")
    String makeseed(@JsonRpcOptional @JsonRpcParam("seed_type") String seed_type);

    /**
     * Generates a new seed. Does not change the current seed.
     *
     * @param seed_type the type of seed to create, e.g. 'standard' or 'segwit'
     * @param language  default language for wordlist
     * @return a new seed
     */
    @JsonRpcMethod("make_seed")
    String makeseed(@JsonRpcOptional @JsonRpcParam("seed_type") String seed_type,
                    @JsonRpcOptional @JsonRpcParam("language") String language);

    /**
     * Return the wallet’s mnemonic seed.
     *
     * @param password     wallet password
     * @param walletPath   wallet path
     * @param forgetconfig forget config on exit
     * @return the wallet's mnemonic seed
     */
    @JsonRpcMethod("getseed")
    String getseed(@JsonRpcOptional @JsonRpcParam("password") String password,
                   @JsonRpcOptional @JsonRpcParam("wallet") String walletPath,
                   @JsonRpcOptional @JsonRpcParam("forgetconfig") Boolean forgetconfig);

    /**
     * return wallet synchronization status
     *
     * @return wallet synchronization status
     */
    @JsonRpcMethod("is_synchronized")
    Boolean issynchronized();

    /**
     * return wallet synchronization status
     *
     * @param walletPath   wallet path
     * @param forgetconfig forget config on exit
     * @return wallet synchronization status
     */
    @JsonRpcMethod("is_synchronized")
    Boolean issynchronized(@JsonRpcOptional @JsonRpcParam("wallet_path") String walletPath,
                           @JsonRpcOptional @JsonRpcParam("forgetconfig") Boolean forgetconfig);

    @JsonRpcMethod("getbalance")
    BalanceResponse getbalance();

    /**
     * Return the balance of your wallet.
     *
     * @param walletPath   wallet path
     * @param forgetconfig forget config on exit
     * @return wallet balance
     */
    @JsonRpcMethod("getbalance")
    BalanceResponse getbalance(@JsonRpcOptional @JsonRpcParam("wallet_path") String walletPath,
                               @JsonRpcOptional @JsonRpcParam("forgetconfig") Boolean forgetconfig);

    /**
     * Check if address is in wallet.
     *
     * @param address      the wallet address
     * @param walletPath   wallet path
     * @param forgetconfig forget config on exit
     * @return true if the address belongs to the wallet, or false otherwise
     */
    @JsonRpcMethod("ismine")
    Boolean ismine(@JsonRpcParam("address") String address,
                   @JsonRpcOptional @JsonRpcParam("wallet_path") String walletPath,
                   @JsonRpcOptional @JsonRpcParam("forgetconfig") Boolean forgetconfig);

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
     * Create a new wallet.
     *
     * @param passphrase         Seed extension
     * @param encryptionPassword Whether the file on disk should be encrypted with the provided password
     * @param seedType           The type of seed to create, e.g. 'standard' or 'segwit'
     * @param password           wallet password
     * @param walletPath         wallet path
     * @param forgetconfig       Forget config on exit
     * @return a new wallet or error in case it exists
     */
    @JsonRpcMethod("create")
    CreateResponse create(@JsonRpcOptional @JsonRpcParam("passphrase") String passphrase,
                          @JsonRpcOptional @JsonRpcParam("encrypt_file") String encryptionPassword,
                          @JsonRpcOptional @JsonRpcParam("seed_type") String seedType,
                          @JsonRpcOptional @JsonRpcParam("password") String password,
                          @JsonRpcOptional @JsonRpcParam("wallet_path") String walletPath,
                          @JsonRpcOptional @JsonRpcParam("forgetconfig") Boolean forgetconfig);

    /**
     * Returns the first unused address of the wallet, or none if all addresses are used.
     * An address is considered to be used if it has received a transaction,
     * or if it is used in a payment request.
     *
     * @return the first unused address of the wallet, or none if all addresses are used.
     */
    @JsonRpcMethod("getunusedaddress")
    @Nullable
    String getunusedaddress();

    @JsonRpcMethod("getunusedaddress")
    @Nullable
    String getunusedaddress(@JsonRpcOptional @JsonRpcParam("wallet_path") String walletPath,
                            @JsonRpcOptional @JsonRpcParam("forgetconfig") Boolean forgetconfig);

    /**
     * Create a new receiving address, beyond the gap limit of the wallet.
     *
     * @return a new receiving address
     */
    @JsonRpcMethod("createnewaddress")
    String createnewaddress();

    @JsonRpcMethod("createnewaddress")
    String createnewaddress(@JsonRpcOptional @JsonRpcParam("wallet_path") String walletPath,
                            @JsonRpcOptional @JsonRpcParam("forgetconfig") Boolean forgetconfig);

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

    @JsonRpcMethod("onchain_history")
    HistoryResponse onchainhistory(@JsonRpcOptional @JsonRpcParam("show_addresses") Boolean showAddresses,
                                   @JsonRpcOptional @JsonRpcParam("year") Long year,
                                   @JsonRpcOptional @JsonRpcParam("from_height") Long fromHeight,
                                   @JsonRpcOptional @JsonRpcParam("to_height") Long toHeight,
                                   @JsonRpcOptional @JsonRpcParam("wallet_path") String walletPath,
                                   @JsonRpcOptional @JsonRpcParam("forgetconfig") Boolean forgetconfig);

    /**
     * List wallets open in daemon
     *
     * @return open wallets in daemon
     */
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

    @JsonRpcMethod("getinfo")
    GetInfoResponse getinfo();

    @JsonRpcMethod("load_wallet")
    Boolean loadwallet();

    /**
     * Load the wallet in memory
     *
     * @param walletPath   wallet path
     * @param password     wallet password
     * @param unlock       unlock the wallet (store the password in memory)
     * @param forgetconfig forget config on exit
     */
    @JsonRpcMethod("load_wallet")
    void loadwallet(@JsonRpcOptional @JsonRpcParam("wallet_path") String walletPath,
                    @JsonRpcOptional @JsonRpcParam("password") String password,
                    @JsonRpcOptional @JsonRpcParam("unlock") Boolean unlock,
                    @JsonRpcOptional @JsonRpcParam("forgetconfig") Boolean forgetconfig);

    /**
     * Close wallet
     */
    @JsonRpcMethod("close_wallet")
    Boolean closewallet();

    /**
     * Close wallet
     *
     * @param walletPath   wallet path
     * @param forgetconfig forget config on exit
     */
    @JsonRpcMethod("close_wallet")
    Boolean closewallet(@JsonRpcOptional @JsonRpcParam("wallet_path") String walletPath,
                        @JsonRpcOptional @JsonRpcParam("forgetconfig") Boolean forgetconfig);


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
            @JsonRpcOptional @JsonRpcParam("rbf") Boolean rbf,
            @JsonRpcOptional @JsonRpcParam("locktime") Long locktime, // untested atm
            @JsonRpcOptional @JsonRpcParam("addtransaction") Object addtransaction, // untested atm
            @JsonRpcOptional @JsonRpcParam("password") String password,
            @JsonRpcOptional @JsonRpcParam("wallet_path") String walletPath,
            @JsonRpcOptional @JsonRpcParam("forgetconfig") Boolean forgetconfig
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
     * Watch an address. Everytime the address changes, a http POST is sent to the URL.
     *
     * @param address the address being watched
     * @param url     the url to POST to when address balances changes
     * @return will always return true or errors (in v3.3.8)
     */
    @JsonRpcMethod("notify")
    Boolean notify(@JsonRpcParam("address") String address,
                   @JsonRpcParam("URL") String url);

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
     * Sign a transaction. The wallet keys will be used to sign the transaction.
     * <p>
     * Password is optional (if you have an unencrypted wallet - which is highly discouraged).
     *
     * @param tx               a raw transaction (hexadecimal)
     * @param password         the wallet passphrase (null if unencrypted).
     * @param walletPath       wallet path
     * @param forgetconfig     forget config on exit
     * @param iknowwhatimdoing acknowledge that I understand the full implications of what I am about to do
     * @return a signed transaction
     */
    @JsonRpcMethod("signtransaction")
    String signtransaction(@JsonRpcParam("tx") String tx,
                           @JsonRpcOptional @JsonRpcParam("password") String password,
                           @JsonRpcOptional @JsonRpcParam("wallet_path") String walletPath,
                           @JsonRpcOptional @JsonRpcParam("forgetconfig") Boolean forgetconfig,
                           @JsonRpcOptional @JsonRpcParam("iknowwhatimdoing") Boolean iknowwhatimdoing);


    /**
     * Stop daemon
     *
     * @return "Daemon stopped" string
     */
    @JsonRpcMethod("stop")
    String stop();

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
     * @param message   Clear text message. Use quotes if it contains spaces.
     * @return true if the signature is valid
     */
    @JsonRpcMethod("verifymessage")
    Boolean verifymessage(@JsonRpcParam("address") String address,
                          @JsonRpcParam("signature") String signature,
                          @JsonRpcParam("message") String message);

    /**
     * Returns the version of Electrum that is currently running.
     *
     * @return the version of Electrum that is currently running
     */
    @JsonRpcMethod("version")
    String version();

    /**
     * Return information about dependencies, such as their version and path.
     * <p>
     * Example:
     * <code>
     * {
     * "aiohttp.version": "3.11.12",
     * "aiorpcx.version": "0.23.1",
     * "certifi.version": "2025.01.31",
     * "cryptodome.version": null,
     * "cryptography.path": "/usr/local/lib/python3.9/site-packages/cryptography",
     * "cryptography.version": "44.0.1",
     * "dnspython.version": "2.7.0",
     * "electrum.path": "/usr/local/lib/python3.9/site-packages/electrum",
     * "electrum.version": "4.5.8",
     * "hidapi.version": null,
     * "libsecp256k1.path": "libsecp256k1.so.2",
     * "libusb.version": null,
     * "libzbar.path": null,
     * "pyaes.version": null,
     * "python.path": "/usr/local/bin/python3.9",
     * "python.version": "3.9.21 (main, Feb 14 2025, 19:14:18) \n[GCC 14.2.0]"
     * }
     * </code>
     *
     * @return information about dependencies
     */
    @JsonRpcMethod("version_info")
    Map<String, String> versioninfo();
}
