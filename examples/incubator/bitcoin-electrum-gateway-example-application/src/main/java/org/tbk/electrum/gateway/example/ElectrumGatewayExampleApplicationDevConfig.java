package org.tbk.electrum.gateway.example;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.params.RegTestParams;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.regtest.electrum.faucet.ElectrumRegtestFaucet;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.common.WalletParams;
import org.tbk.electrum.rpc.command.CreateNewAddressParams;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
@Configuration(proxyBeanMethods = false)
@Profile("development")
class ElectrumGatewayExampleApplicationDevConfig {

    @Bean
    InitializingBean sendToDefaultWallet(ElectrumRegtestFaucet faucet,
                                         ElectrumClient electrumClient,
                                         WalletParams defaultWalletParams) {
        return new FundDefaultWalletLoop(faucet, () -> {
            String newAddress = electrumClient.createNewAddress(CreateNewAddressParams.builder()
                    .walletPath(defaultWalletParams.getWalletPath())
                    .build());
            return Address.fromString(RegTestParams.get(), newAddress);
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
                            }
                    )
                    .subscribe();

            Runtime.getRuntime().addShutdownHook(new Thread(subscription::dispose));
        }
    }
}
