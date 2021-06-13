package org.tbk.bitcoin.example.payreq;

import com.msgilligan.bitcoinj.json.pojo.BlockChainInfo;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import com.msgilligan.bitcoinj.rpc.BitcoinExtendedClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.javamoney.moneta.Money;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.GetInfoResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.example.payreq.common.Currencies;
import org.tbk.bitcoin.regtest.BitcoindRegtestTestHelper;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import javax.money.CurrencyUnit;
import javax.money.convert.ConversionQueryBuilder;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.ExchangeRate;
import javax.money.convert.MonetaryConversions;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class BitcoinPaymentRequestExampleApplicationConfig {

    /**
     * We must have access to a wallet for "getnewaddress" command to work.
     * Create a wallet if none is found (currently only when in regtest mode)!
     * Maybe move to {@link org.tbk.bitcoin.regtest.config.BitcoinRegtestAutoConfiguration}?
     */
    @Bean
    public InitializingBean createWalletIfMissing(BitcoinExtendedClient bitcoinRegtestClient) {
        return () -> BitcoindRegtestTestHelper.createDefaultWalletIfNecessary(bitcoinRegtestClient);
    }

    @Bean
    @Profile("!test")
    public ApplicationRunner mainRunner(SynchronousLndAPI lndApi) {
        return args -> {
            GetInfoResponse info = lndApi.getInfo();
            log.info("=================================================");
            log.info("[lnd] identity_pubkey: {}", info.getIdentityPubkey());
            log.info("[lnd] alias: {}", info.getAlias());
            log.info("[lnd] version: {}", info.getVersion());
        };
    }

    @Bean
    @Profile("!test")
    public ApplicationRunner lndBestBlockLogger(SynchronousLndAPI lndApi,
                                                MessagePublishService<Block> bitcoinBlockPublishService) {
        return args -> {
            bitcoinBlockPublishService.awaitRunning(Duration.ofSeconds(20));
            Disposable subscription = Flux.from(bitcoinBlockPublishService).subscribe(val -> {
                try {
                    GetInfoResponse info = lndApi.getInfo();
                    log.info("=================================================");
                    log.info("[lnd] block height: {}", info.getBlockHeight());
                    log.info("[lnd] block hash: {}", info.getBlockHash());
                    log.info("[lnd] best header timestamp: {}", info.getBestHeaderTimestamp());
                } catch (StatusException | ValidationException e) {
                    log.error("", e);
                }
            });

            Runtime.getRuntime().addShutdownHook(new Thread(subscription::dispose));
        };
    }

    @Bean
    @Profile("!test")
    public ApplicationRunner bestBlockLogger(BitcoinClient bitcoinJsonRpcClient,
                                             MessagePublishService<Block> bitcoinBlockPublishService) {
        return args -> {
            bitcoinBlockPublishService.awaitRunning(Duration.ofSeconds(20));
            Disposable subscription = Flux.from(bitcoinBlockPublishService).subscribe(val -> {
                try {
                    BlockChainInfo info = bitcoinJsonRpcClient.getBlockChainInfo();
                    log.info("[bitcoind] new best block (height: {}): {}", info.getBlocks(), info.getBestBlockHash());
                } catch (IOException e) {
                    log.error("", e);
                }
            });

            Runtime.getRuntime().addShutdownHook(new Thread(subscription::dispose));
        };
    }

    /**
     * Initialize Currency Conversion
     * <p>
     * Load exchange rates of some fiat to BTC pairs.
     */
    @Bean
    public InitializingBean initCurrencyConversion() {
        BiFunction<CurrencyUnit, CurrencyUnit, Optional<ExchangeRate>> loadExchangeRate = (base, term) -> {
            Money singleBaseAmount = Money.of(BigDecimal.ONE, base);
            Money singleTermAmount = Money.of(BigDecimal.ONE, term);

            try {
                CurrencyConversion baseToTermConversion = MonetaryConversions.getConversion(ConversionQueryBuilder.of()
                        .setBaseCurrency(base)
                        .setTermCurrency(term)
                        .build());

                ExchangeRate exchangeRate = baseToTermConversion.getExchangeRate(singleTermAmount);

                Money singleFiatAmountInBtc = singleBaseAmount.with(baseToTermConversion);
                log.info("{} equals {}", singleBaseAmount, singleFiatAmountInBtc);

                return Optional.of(exchangeRate);
            } catch (Exception e) {
                log.warn("Error occurred during currency conversion of {} to {}: {}", singleBaseAmount, term, e.getMessage());
                return Optional.empty();
            }
        };

        return () -> {
            List<CurrencyUnit> fiatCurrencyUnits = List.of(Currencies.EUR, Currencies.USD);

            fiatCurrencyUnits.forEach(fiatCurrencyUnit -> {
                log.info("trying to load exchange rate and inverse exchange rate for {}/{}", fiatCurrencyUnit, Currencies.BTC);
                loadExchangeRate.apply(fiatCurrencyUnit, Currencies.BTC);
                loadExchangeRate.apply(Currencies.BTC, fiatCurrencyUnit);
            });
        };
    }
}
