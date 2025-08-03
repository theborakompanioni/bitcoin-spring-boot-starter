package org.tbk.electrum.gateway.example.job;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.params.RegTestParams;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.regtest.electrum.faucet.ElectrumRegtestFaucet;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.common.WalletParams;
import org.tbk.electrum.model.SimpleTxoValue;
import org.tbk.electrum.rpc.command.AddRequestParams;
import org.tbk.electrum.rpc.command.AddRequestResponse;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Configuration(proxyBeanMethods = false)
@Profile("development")
class FundWalletJobConfig {

    @DisallowConcurrentExecution
    @RequiredArgsConstructor
    public static class FundWalletJob implements Job {
        private final AtomicLong counter = new AtomicLong(0);

        @NonNull
        private final ElectrumRegtestFaucet faucet;

        @NonNull
        private final ElectrumClient client;

        @NonNull
        private final WalletParams walletParams;

        @Override
        public void execute(JobExecutionContext context) {
            Coin coins = Optional.of(context.getMergedJobDataMap())
                    .map(it -> it.getString("amount"))
                    .map(Long::parseLong)
                    .map(Coin::valueOf)
                    .orElseGet(() -> Coin.valueOf(21_000 + counter.getAndIncrement()));

            Duration timeout = Optional.of(context.getMergedJobDataMap())
                    .map(it -> it.getString("timeout"))
                    .map(Duration::parse)
                    .orElseGet(() -> Duration.ofSeconds(60));

            AddRequestResponse result = client.addRequest(AddRequestParams.builder()
                    .amount(SimpleTxoValue.zero())
                    .expiry(Duration.ofSeconds(60))
                    .walletPath(walletParams.getWalletPath())
                    .build());
            Address address = Address.fromString(RegTestParams.get(), result.getAddress());

            Sha256Hash txid = faucet.requestBitcoin(() -> address, coins).block(timeout);
            log.info("[DEV] Sent {} to {} ({}) in {}", coins, address, walletParams.getWalletPath(), txid);
        }
    }

    @Bean
    FundWalletJob fundWalletJob(ElectrumRegtestFaucet faucet,
                                ElectrumClient client,
                                WalletParams defaultWalletParams) {
        return new FundWalletJob(faucet, client, defaultWalletParams);
    }

    @Bean
    JobDetail fundWalletJobDetail() {
        return JobBuilder.newJob()
                .ofType(FundWalletJob.class)
                .storeDurably()
                .withIdentity("FundWalletJob")
                .withDescription("Fund wallet from regtest faucet.")
                .build();
    }
}
