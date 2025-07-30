package org.tbk.electrum.gateway.example.watch;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.ConfigKeyEnum;

@Slf4j
@RequiredArgsConstructor
public class LogElectrumConfig implements CommandLineRunner {

    @NonNull
    private final ElectrumClient client;

    @Override
    public void run(String... args) {
        printConfig(ConfigKeyEnum.fee_policy_default);
        printConfig(ConfigKeyEnum.network_skipmerklecheck);
        printConfig(ConfigKeyEnum.wallet_spend_confirmed_only);
        printConfig(ConfigKeyEnum.wallet_freeze_reused_address_utxos);
        printConfig(ConfigKeyEnum.wallet_coin_chooser_output_rounding);
        printConfig(ConfigKeyEnum.log_to_file);

        printConfig(ConfigKeyEnum.network_proxy_enabled);
        printConfig(ConfigKeyEnum.network_proxy);
        printConfig(ConfigKeyEnum.network_proxy_user);
        printConfig(ConfigKeyEnum.network_proxy_password);
    }

    private void printConfig(ConfigKeyEnum key) {
        log.info("config '{}': {}", key.getKey().getKey(), this.client.getConfig(key).orElse(null));
    }
}
