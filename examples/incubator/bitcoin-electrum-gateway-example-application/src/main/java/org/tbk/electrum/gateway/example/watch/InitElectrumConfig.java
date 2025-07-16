package org.tbk.electrum.gateway.example.watch;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.ConfigKeyEnum;

@Slf4j
@RequiredArgsConstructor
public class InitElectrumConfig implements InitializingBean {

    @NonNull
    private final ElectrumClient client;

    @Override
    public void afterPropertiesSet() {
        this.client.setConfig(ConfigKeyEnum.fee_policy_default, "eta:1");
        this.client.setConfig(ConfigKeyEnum.network_skipmerklecheck, Boolean.TRUE.toString());
        this.client.setConfig(ConfigKeyEnum.wallet_spend_confirmed_only, Boolean.FALSE.toString());
        this.client.setConfig(ConfigKeyEnum.wallet_freeze_reused_address_utxos, Boolean.FALSE.toString());
        this.client.setConfig(ConfigKeyEnum.wallet_coin_chooser_output_rounding, Boolean.FALSE.toString());

        // call to "Abstract_Wallet.get_full_history" logs every access - logs can grow quite large
        this.client.setConfig(ConfigKeyEnum.log_to_file, Boolean.FALSE.toString());

        printConfig(ConfigKeyEnum.fee_policy_default);
        printConfig(ConfigKeyEnum.network_skipmerklecheck);
        printConfig(ConfigKeyEnum.wallet_spend_confirmed_only);
        printConfig(ConfigKeyEnum.wallet_freeze_reused_address_utxos);
        printConfig(ConfigKeyEnum.wallet_coin_chooser_output_rounding);
    }

    private void printConfig(ConfigKeyEnum key) {
        log.info("config '{}': {}", key.getKey().getKey(), this.client.getConfig(key).orElse(null));
    }
}