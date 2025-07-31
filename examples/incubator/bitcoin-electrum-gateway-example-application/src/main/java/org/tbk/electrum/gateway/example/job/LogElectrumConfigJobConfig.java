package org.tbk.electrum.gateway.example.job;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.ConfigKeyEnum;

@Slf4j
@Configuration(proxyBeanMethods = false)
class LogElectrumConfigJobConfig {

    @RequiredArgsConstructor
    public static class ElectrumConfigLoggingJob implements Job {
        @NonNull
        private ElectrumClient client;

        @Override
        public void execute(JobExecutionContext context) {
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

    @Bean
    ElectrumConfigLoggingJob electrumConfigLoggingJob(ElectrumClient electrumClient) {
        return new ElectrumConfigLoggingJob(electrumClient);
    }

    @Bean
    JobDetail electrumConfigLoggingJobDetail() {
        return JobBuilder.newJob()
                .ofType(ElectrumConfigLoggingJob.class)
                .storeDurably()
                .withIdentity("ElectrumConfigLoggingJob")
                .withDescription("Fetch and log electrum config.")
                .build();
    }
}
