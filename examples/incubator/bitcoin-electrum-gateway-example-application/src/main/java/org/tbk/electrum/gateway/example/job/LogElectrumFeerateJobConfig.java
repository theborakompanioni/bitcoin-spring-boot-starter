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

@Slf4j
@Configuration(proxyBeanMethods = false)
class LogElectrumFeerateJobConfig {

    @RequiredArgsConstructor
    public static class ElectrumFeerateLoggingJob implements Job {
        @NonNull
        private ElectrumClient electrumClient;

        public void execute(JobExecutionContext context) {
            log.info("Electrum fee rate: {}", electrumClient.getFeerate());
        }
    }

    @Bean
    ElectrumFeerateLoggingJob electrumFeerateLoggingJob(ElectrumClient electrumClient) {
        return new ElectrumFeerateLoggingJob(electrumClient);
    }

    @Bean
    JobDetail electrumFeerateLoggingJobDetail() {
        return JobBuilder.newJob()
                .ofType(ElectrumFeerateLoggingJob.class)
                .storeDurably()
                .withIdentity("ElectrumFeerateLoggingJob")
                .withDescription("Fetch and log electrum feerate...")
                .build();
    }
}
