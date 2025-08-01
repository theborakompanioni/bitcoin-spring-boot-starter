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
import org.tbk.bitcoin.regtest.electrum.common.ElectrumdStatusLogging;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.common.WalletParams;

@Slf4j
@Configuration(proxyBeanMethods = false)
class LogElectrumStatusJobConfig {

    @RequiredArgsConstructor
    public static class ElectrumStatusLoggingJob implements Job {
        @NonNull
        private ElectrumClient client;

        @NonNull
        private WalletParams walletParams;

        @Override
        public void execute(JobExecutionContext context) {
            ElectrumdStatusLogging.logStatus(client, walletParams);
        }
    }

    @Bean
    ElectrumStatusLoggingJob electrumStatusLoggingJob(ElectrumClient electrumClient,
                                                      WalletParams walletParams) {
        return new ElectrumStatusLoggingJob(electrumClient, walletParams);
    }

    @Bean
    JobDetail electrumStatusLoggingJobDetail() {
        return JobBuilder.newJob()
                .ofType(ElectrumStatusLoggingJob.class)
                .storeDurably()
                .withIdentity("ElectrumStatusLoggingJob")
                .withDescription("Fetch and log electrum status.")
                .build();
    }
}
