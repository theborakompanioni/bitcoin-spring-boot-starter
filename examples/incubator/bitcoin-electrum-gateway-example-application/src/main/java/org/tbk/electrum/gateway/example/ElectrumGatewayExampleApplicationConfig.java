package org.tbk.electrum.gateway.example;

import com.google.common.util.concurrent.AbstractScheduledService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.common.WalletParams;
import org.tbk.electrum.gateway.example.watch.ElectrumDaemonWalletSendBalance;
import org.tbk.electrum.gateway.example.watch.ElectrumWalletWatchLoop;
import org.tbk.electrum.gateway.example.watch.InitElectrumConfig;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static org.tbk.bitcoin.regtest.common.BitcoindStatusLogging.logBitcoinStatusOnNewBlock;
import static org.tbk.bitcoin.regtest.electrum.common.ElectrumdStatusLogging.logElectrumStatusOnNewBlock;

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
    InitElectrumConfig initElectrumConfig(ElectrumClient electrumClient) {
        return new InitElectrumConfig(electrumClient);
    }

    @Bean
    @Profile("!test")
    CommandLineRunner logBitcoinStatus(MessagePublishService<Block> bitcoinjBlockPublishService,
                                       BitcoinClient bitcoinClient) {
        return args -> logBitcoinStatusOnNewBlock(bitcoinjBlockPublishService, bitcoinClient);
    }

    @Bean
    @Profile("!test")
    @ConditionalOnBean(WalletParams.class)
    CommandLineRunner logElectrumStatus(MessagePublishService<Block> bitcoinjBlockPublishService,
                                        ElectrumClient electrumClient,
                                        WalletParams walletParams) {
        return args -> logElectrumStatusOnNewBlock(bitcoinjBlockPublishService, electrumClient, walletParams);
    }

    @Bean
    public static BeanFactoryPostProcessor electrumGatewayBeanFactoryPostProcessor() {
        return beanFactory -> {
            beanFactory.addBeanPostProcessor(new ElectrumGatewayBeanFactoryPostProcessor(beanFactory));
        };
    }

    @RequiredArgsConstructor
    public static class ElectrumGatewayBeanFactoryPostProcessor implements BeanPostProcessor {

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
                    log.debug("Skip creating bean '{}' - factory already contains a bean with the same name", beanName);
                } else {
                    ElectrumWalletWatchLoop bean = createElectrumWalletWatchLoop(properties, electrumClient, walletParams);
                    InitializingBean initHook = bean::startAsync;
                    DisposableBean destroyHook = bean::stopAsync;

                    beanFactory.registerSingleton(beanName, bean);
                    beanFactory.registerSingleton(beanName + "InitHook", initHook);
                    beanFactory.registerSingleton(beanName + "DestroyHook", destroyHook);

                    beanFactory.initializeBean(bean, beanName);
                    beanFactory.initializeBean(destroyHook, beanName + "DestroyHook");
                    beanFactory.initializeBean(initHook, beanName + "InitHook");
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

            ElectrumDaemonWalletSendBalance.Options options = ElectrumDaemonWalletSendBalance.Options.builder()
                    .walletParams(WalletParams.builder()
                            .walletPath(walletEntry.getWalletPath())
                            .password(walletEntry.getPassword().orElse(null))
                            .build())
                    .destinationAddress(properties.getDestinationAddress())
                    .build();

            return new ElectrumWalletWatchLoop(electrumClient, options, scheduler);
        }
    }
}
