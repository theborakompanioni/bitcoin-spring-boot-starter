package org.tbk.electrum.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

// A small, predefined subset of available config keys
// see https://github.com/spesmilo/electrum/blob/4.6.0b1/electrum/simple_config.py#L609
@Getter
@RequiredArgsConstructor
public enum ConfigKeyEnum {
    network_proxy(ConfigKey.of("proxy")),
    network_proxy_user(ConfigKey.of("proxy_user")),
    network_proxy_password(ConfigKey.of("proxy_password")),
    network_proxy_enabled(ConfigKey.of("enable_proxy")),
    network_skipmerklecheck(ConfigKey.of("skipmerklecheck")),
    network_server(ConfigKey.of("server")),
    network_offline(ConfigKey.of("offline")),
    network_auto_connect(ConfigKey.of("auto_connect")),
    network_oneserver(ConfigKey.of("oneserver")),
    network_timeout(ConfigKey.of("network_timeout")),

    rpc_user(ConfigKey.of("rpcuser")),
    rpc_password(ConfigKey.of("rpcpassword")),
    rpc_host(ConfigKey.of("rpchost")),
    rpc_port(ConfigKey.of("rpcport")),
    rpc_socket_type(ConfigKey.of("rpcsock")),
    rpc_socket_filepath(ConfigKey.of("rpcsockpath")),

    fee_policy_default(ConfigKey.of("fee_policy.default")),
    fee_policy_lightning(ConfigKey.of("fee_policy.lnwatcher")),
    fee_policy_swaps(ConfigKey.of("fee_policy.'fee_policy.swaps'")),

    log_to_file(ConfigKey.of("log_to_file")),
    logs_num_files_keep(ConfigKey.of("logs_num_files_keep")),

    wallet_spend_confirmed_only(ConfigKey.of("confirmed_only")),
    wallet_freeze_reused_address_utxos(ConfigKey.of("wallet_freeze_reused_address_utxos")),
    wallet_coin_chooser_output_rounding(ConfigKey.of("coin_chooser_output_rounding")),

    check_updates(ConfigKey.of("check_updates")),
    dont_show_testnet_warning(ConfigKey.of("dont_show_testnet_warning"));

    @NonNull
    ConfigKey key;
}
