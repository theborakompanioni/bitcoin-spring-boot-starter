package org.tbk.electrum.model;

// see https://github.com/spesmilo/electrum/blob/4.6.0b1/electrum/simple_config.py#L609
public enum ConfigKey {
    auto_connect,
    oneserver,
    check_updates,
    config_version,
    confirmed_only,
    @Deprecated
    dynamic_fees,
    @Deprecated
    fee_level,
    @Deprecated
    fee_per_kb,
    @Deprecated
    use_rbf,
    @Deprecated
    batch_rbf,
    log_to_file,
    @Deprecated
    coin_chooser_output_rounding,
    rpcuser,
    rpcpassword,
    dont_show_testnet_warning
}
