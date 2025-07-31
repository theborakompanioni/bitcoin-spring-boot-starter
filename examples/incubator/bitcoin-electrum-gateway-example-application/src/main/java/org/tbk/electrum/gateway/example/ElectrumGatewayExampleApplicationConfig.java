package org.tbk.electrum.gateway.example;

import com.google.common.util.concurrent.AbstractScheduledService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.common.WalletParams;
import org.tbk.electrum.gateway.example.watch.ElectrumDaemonWalletSendBalance;
import org.tbk.electrum.gateway.example.watch.ElectrumWalletWatchLoop;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static org.quartz.SimpleScheduleBuilder.repeatSecondlyForever;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ElectrumGatewayExampleApplicationProperties.class)
class ElectrumGatewayExampleApplicationConfig {

    private final ElectrumGatewayExampleApplicationProperties properties;

    public ElectrumGatewayExampleApplicationConfig(ElectrumGatewayExampleApplicationProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    WalletParams defaultWalletParams() {
        return properties.getWallets().values().stream().findFirst()
                .map(it -> WalletParams.builder()
                        .walletPath(it.getWalletPath())
                        .password(it.getPassword().orElse(null))
                        .build())
                .orElseThrow(() -> new IllegalStateException("At least one wallet must be specified."));
    }

    @Bean
    @Profile("!test")
    Trigger electrumConfigLoggingJobTrigger(JobDetail electrumConfigLoggingJobDetail) {
        return TriggerBuilder.newTrigger().forJob(electrumConfigLoggingJobDetail)
                .withIdentity("electrumConfigLoggingJobTrigger")
                .withDescription("Trigger ElectrumConfigLoggingJob once")
                .withSchedule(simpleSchedule().withRepeatCount(0))
                .startNow()
                .build();
    }

    @Bean
    @Profile("!test")
    Trigger electrumStatusLoggingJobTrigger(JobDetail electrumStatusLoggingJobDetail) {
        return TriggerBuilder.newTrigger().forJob(electrumStatusLoggingJobDetail)
                .withIdentity("ElectrumStatusLoggingJobTrigger")
                .withDescription("Trigger ElectrumStatusLoggingJob every 60s")
                .withSchedule(repeatSecondlyForever(60))
                .startNow()
                .build();
    }

    @Bean
    @Profile("!test")
    Trigger electrumFeerateLoggingJobTrigger(JobDetail electrumFeerateLoggingJobDetail) {
        return TriggerBuilder.newTrigger().forJob(electrumFeerateLoggingJobDetail)
                .withIdentity("ElectrumFeerateLoggingJob")
                .withDescription("Trigger ElectrumFeerateLoggingJob every 30s")
                .withSchedule(repeatSecondlyForever(30))
                .startNow()
                .build();
    }

    @Bean
    public static BeanFactoryPostProcessor createElectrumWalletWatchLoopsPostProcessor() {
        return beanFactory -> {
            beanFactory.addBeanPostProcessor(new CreateElectrumWalletWatchLoopsPostProcessor(beanFactory));
        };
    }

    @RequiredArgsConstructor
    public static class CreateElectrumWalletWatchLoopsPostProcessor implements BeanPostProcessor {

        @NonNull
        private final ConfigurableListableBeanFactory beanFactory;

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof ElectrumGatewayExampleApplicationProperties properties) {
                createElectrumWalletWatchLoops(properties);
            }

            return bean;
        }

        private void createElectrumWalletWatchLoops(ElectrumGatewayExampleApplicationProperties properties) {
            ElectrumClient electrumClient = beanFactory.getBean(ElectrumClient.class);
            properties.getWallets().forEach((name, walletParams) -> {
                log.info("Create watch loop for wallet '{}': {}", name, walletParams.getWalletPath());
                String beanName = name + "ElectrumWalletWatchLoop";
                if (beanFactory.containsBean(beanName)) {
                    log.warn("Skip creating bean '{}' - factory already contains a bean with the same name", beanName);
                } else {
                    ElectrumWalletWatchLoop bean = createElectrumWalletWatchLoop(properties, electrumClient, walletParams);
                    CommandLineRunner initHook = args -> bean.startAsync();
                    DisposableBean destroyHook = bean::stopAsync;

                    beanFactory.registerSingleton(beanName, bean);
                    beanFactory.registerSingleton(beanName + "InitHook", initHook);
                    beanFactory.registerSingleton(beanName + "DestroyHook", destroyHook);

                    beanFactory.initializeBean(bean, beanName);
                    beanFactory.initializeBean(destroyHook, beanName + "DestroyHook");
                    beanFactory.initializeBean(initHook, beanName + "InitHook");
                    log.info("Successfully created watch loop for wallet '{}': {}", name, walletParams.getWalletPath());
                }
            });
        }

        private ElectrumWalletWatchLoop createElectrumWalletWatchLoop(ElectrumGatewayExampleApplicationProperties properties,
                                                                      ElectrumClient electrumClient,
                                                                      ElectrumGatewayExampleApplicationProperties.WalletEntry walletEntry) {
            AbstractScheduledService.Scheduler scheduler = AbstractScheduledService.Scheduler.newFixedDelaySchedule(
                    properties.getInitialDelay().toNanos(),
                    properties.getDelay().toNanos(),
                    TimeUnit.NANOSECONDS
            );

            ElectrumDaemonWalletSendBalance.Options sendBalanceOptions = ElectrumDaemonWalletSendBalance.Options.builder()
                    .walletParams(WalletParams.builder()
                            .walletPath(walletEntry.getWalletPath())
                            .password(walletEntry.getPassword().orElse(null))
                            .build())
                    .destinationAddress(properties.getDestinationAddress())
                    .build();

            ElectrumWalletWatchLoop.Options options = ElectrumWalletWatchLoop.Options.builder()
                    .sendBalanceOptions(sendBalanceOptions)
                    .gapLimit(walletEntry.getGapLimit().orElse(null))
                    .build();

            return new ElectrumWalletWatchLoop(electrumClient, options, scheduler);
        }
    }
}
