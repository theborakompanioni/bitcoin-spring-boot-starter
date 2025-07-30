package org.tbk.electrum.gateway.example;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Coin;
import org.bitcoinj.params.RegTestParams;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.regtest.electrum.faucet.ElectrumRegtestFaucet;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.common.WalletParams;
import org.tbk.electrum.model.SimpleTxoValue;
import org.tbk.electrum.rpc.command.AddRequestParams;
import org.tbk.electrum.rpc.command.AddRequestResponse;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.function.Supplier;

import static org.tbk.bitcoin.regtest.common.BitcoindStatusLogging.logBitcoinStatusOnNewBlock;
import static org.tbk.bitcoin.regtest.electrum.common.ElectrumdStatusLogging.logElectrumStatusOnNewBlock;

@Slf4j
@Configuration(proxyBeanMethods = false)
@Profile("development")
class ElectrumGatewayExampleApplicationDevConfig {

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
    InitializingBean sendToDefaultWallet(ElectrumRegtestFaucet faucet,
                                         ElectrumClient electrumClient,
                                         WalletParams defaultWalletParams) {
        return new FundDefaultWalletLoop(faucet, () -> {
            AddRequestResponse result = electrumClient.addRequest(AddRequestParams.builder()
                    .amount(SimpleTxoValue.zero())
                    .expiry(Duration.ofSeconds(60))
                    .walletPath(defaultWalletParams.getWalletPath())
                    .build());
            return Address.fromString(RegTestParams.get(), result.getAddress());
        });
    }

    @RequiredArgsConstructor
    public static class FundDefaultWalletLoop implements InitializingBean {

        @NonNull
        private final ElectrumRegtestFaucet faucet;

        @NonNull
        private final Supplier<Address> destinationAddress;

        @Override
        public void afterPropertiesSet() {
            Disposable subscription = Flux.interval(Duration.ofSeconds(1), Duration.ofSeconds(21))
                    .publishOn(Schedulers.boundedElastic())
                    .map(it -> Coin.valueOf(21_000 + it))
                    .flatMap(coins -> {
                        Address address = destinationAddress.get();
                        return faucet.requestBitcoin(() -> address, coins)
                                .doOnNext(txid -> log.info("[DEV] Sent {} to {} in {}", coins, address, txid));
                    })
                    .retry()
                    .onErrorResume(e -> {
                        log.info("[DEV] error while sending from faucet: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .subscribe();

            Runtime.getRuntime().addShutdownHook(new Thread(subscription::dispose));
        }
    }
}
