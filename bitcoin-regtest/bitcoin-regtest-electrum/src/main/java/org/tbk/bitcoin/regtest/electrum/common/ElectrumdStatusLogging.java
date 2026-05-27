package org.tbk.bitcoin.regtest.electrum.common;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.bitcoinj.base.Coin;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.common.WalletParams;
import org.tbk.electrum.model.Balance;
import org.tbk.electrum.model.OnchainHistory;
import org.tbk.electrum.model.TxoValue;
import org.tbk.electrum.rpc.command.GetBalanceParams;
import org.tbk.electrum.rpc.command.GetInfoResponse;
import org.tbk.electrum.rpc.command.IsSynchronizedParams;
import org.tbk.electrum.rpc.command.OnchainHistoryParams;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
public final class ElectrumdStatusLogging {

    private ElectrumdStatusLogging() {
        throw new UnsupportedOperationException();
    }

    public static Disposable logElectrumStatusOnNewBlock(MessagePublishService<Block> bitcoinjBlockPublishService,
                                                         ElectrumClient electrumClient,
                                                         WalletParams walletParams) throws TimeoutException {
        Disposable subscription = Flux.from(bitcoinjBlockPublishService)
                .subscribe(arg -> logStatus(electrumClient, walletParams));

        Runtime.getRuntime().addShutdownHook(new Thread(subscription::dispose));

        bitcoinjBlockPublishService.awaitRunning(Duration.ofSeconds(10));

        return subscription;
    }

    public static void logStatus(ElectrumClient electrumClient,
                                 WalletParams walletParams) {
        try {
            GetInfoResponse info = electrumClient.getInfo();
            Boolean walletSynchronized = electrumClient.isWalletSynchronized(IsSynchronizedParams.builder()
                    .walletPath(walletParams.getWalletPath())
                    .build());

            log.info("============================");
            log.info("Electrum Daemon ({}) Status", info.getVersion());
            log.info("Connected: {}", info.isConnected());
            log.info("Blockheight: {}/{}", info.getBlockchainHeight(), info.getServerHeight());
            log.info("Wallet: {}", walletParams.getWalletPath());
            log.info("Wallet synchronized: {}", walletSynchronized);
            if (Boolean.TRUE.equals(walletSynchronized)) {
                OnchainHistory history = electrumClient.getOnchainHistory(OnchainHistoryParams.builder()
                        .walletPath(walletParams.getWalletPath())
                        .build());

                log.info("Transactions: {}", history.getTransactions().size());

                Balance balance = electrumClient.getBalance(GetBalanceParams.builder()
                        .walletPath(walletParams.getWalletPath())
                        .build());

                log.info("Balance: {} total", friendlyBtcString(balance.getTotal()));
                log.info("         {} confirmed", friendlyBtcString(balance.getConfirmed()));
                log.info("         {} unconfirmed", friendlyBtcString(balance.getUnconfirmed()));
                log.info("         {} spendable", friendlyBtcString(balance.getSpendable()));
                log.info("         {} unmatured", friendlyBtcString(balance.getUnmatured()));
            }
            log.info("============================");
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private static String friendlyBtcString(TxoValue txoValue) {
        return Coin.valueOf(txoValue.getValue()).toFriendlyString();
    }
}
