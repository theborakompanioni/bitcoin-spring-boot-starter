package org.tbk.bitcoin.autodca.example.command;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.kraken.service.KrakenAccountService;
import org.knowm.xchange.kraken.service.KrakenMarketDataService;
import org.springframework.boot.ApplicationArguments;
import org.tbk.bitcoin.autodca.example.BitcoinAutoDcaExampleApplicationConfig.DryRunOption;
import org.tbk.bitcoin.autodca.example.BitcoinAutoDcaExampleProperties;
import org.tbk.bitcoin.format.BitcoinAmountFormatProvider;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
public class KrakenRebalanceCommandRunner extends ConditionalOnNonOptionApplicationRunner {

    private static final CurrencyUnit bitcoinCurrencyUnit = Monetary.getCurrency("BTC");
    private static final MonetaryAmountFormat moneyFormat = MonetaryFormats
            .getAmountFormat(BitcoinAmountFormatProvider.formatNameBitcoin());

    private final KrakenExchange exchange;
    private final BitcoinAutoDcaExampleProperties properties;
    private final DryRunOption dryRun;

    public KrakenRebalanceCommandRunner(KrakenExchange exchange,
                                        BitcoinAutoDcaExampleProperties properties,
                                        DryRunOption dryRun) {
        super("rebalance");
        this.exchange = requireNonNull(exchange);
        this.properties = requireNonNull(properties);
        this.dryRun = requireNonNull(dryRun);
    }

    @Override
    @SneakyThrows
    protected void doRun(ApplicationArguments args) {
        log.debug("Calculate rebalance on exchange {}", exchange);

        if (!dryRun.isEnabled()) {
            throw new IllegalStateException("Currently only implemented with '--dry-run' option");
        }

        Currency bitcoin = Currency.BTC;
        Currency fiatCurrency = Currency.getInstance(properties.getFiatCurrency());

        CurrencyPair currencyPair = new CurrencyPair(bitcoin, fiatCurrency);
        boolean supportedCurrencyPair = exchange.getExchangeSymbols().contains(currencyPair);
        if (!supportedCurrencyPair) {
            throw new IllegalStateException("Currency pair is not supported: " + currencyPair);
        }

        // ---------------------------------------------- balance
        KrakenAccountService accountService = (KrakenAccountService) exchange.getAccountService();
        Map<String, BigDecimal> krakenBalance = accountService.getKrakenBalance();

        Map<String, BigDecimal> krakenPositiveBalances = krakenBalance.entrySet().stream()
                .filter(it -> it.getValue().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (krakenPositiveBalances.isEmpty()) {
            System.out.println("❌ There is no currency pair with a positive balance.");
            return;
        }

        BigDecimal bitcoinBalance = bitcoin.getCurrencyCodes().stream()
                .filter(it -> krakenBalance.containsKey("X" + it))
                .map(it -> krakenBalance.get("X" + it))
                .findFirst()
                .or(() -> Optional.ofNullable(krakenBalance.get(bitcoin.getCurrencyCode())))
                .orElse(BigDecimal.ZERO);

        BigDecimal fiatBalance = Optional.ofNullable(krakenBalance.get("Z" + fiatCurrency.getCurrencyCode()))
                .or(() -> Optional.ofNullable(krakenBalance.get(fiatCurrency.getCurrencyCode())))
                .orElse(BigDecimal.ZERO);
        // ---------------------------------------------- balance - end

        // ---------------------------------------------- ask/bid
        KrakenMarketDataService marketDataService = (KrakenMarketDataService) exchange.getMarketDataService();
        Ticker ticker = marketDataService.getTicker(currencyPair);
        // ---------------------------------------------- ask/bid - end

        BigDecimal oneBitcoinInFiat = ticker.getAsk();
        System.out.printf("📎 1 BTC = %s %s%n", oneBitcoinInFiat, fiatCurrency);

        BigDecimal bitcoinInFiat = bitcoinBalance.multiply(oneBitcoinInFiat).setScale(4, RoundingMode.DOWN);
        BigDecimal totalInFiat = fiatBalance.add(bitcoinInFiat).setScale(4, RoundingMode.DOWN);
        BigDecimal fiatInBitcoin = fiatBalance.divide(oneBitcoinInFiat, 8, RoundingMode.HALF_UP);
        BigDecimal totalInBitcoin = bitcoinBalance.add(fiatInBitcoin).setScale(8, RoundingMode.DOWN);

        BigDecimal percentageOfBalanceInBtc = bitcoinBalance.divide(totalInBitcoin, RoundingMode.HALF_UP)
                .setScale(4, RoundingMode.HALF_UP)
                .movePointRight(2);
        System.out.printf("💰 Balance BTC (%s%%): %s (%s %s)%n",
                percentageOfBalanceInBtc,
                moneyFormat.format(Money.of(bitcoinBalance, bitcoinCurrencyUnit)),
                bitcoinInFiat, fiatCurrency);

        BigDecimal percentageOfBalanceInFiat = fiatInBitcoin.divide(totalInBitcoin, RoundingMode.HALF_UP)
                .setScale(4, RoundingMode.HALF_UP)
                .movePointRight(2);
        System.out.printf("💰 Balance FIAT (%s%%): %s (%s %s)%n",
                percentageOfBalanceInFiat,
                moneyFormat.format(Money.of(fiatBalance, fiatCurrency.getCurrencyCode())),
                fiatInBitcoin, bitcoin);

        System.out.printf("   (_Hypothetical_ Total Balances: %s %s or %s %s)%n", totalInBitcoin, bitcoin, totalInFiat, fiatCurrency);

        BigDecimal multiplicand = new BigDecimal("0.5");
        BigDecimal bitcoinTargetBalance = totalInBitcoin.multiply(multiplicand).setScale(8, RoundingMode.DOWN);

        System.out.printf("📎 Target Balance BTC (%s%%): %s %s%n", multiplicand.multiply(BigDecimal.valueOf(100)),
                bitcoinTargetBalance, bitcoin);

        BigDecimal missingBitcoin = bitcoinTargetBalance.subtract(bitcoinBalance)
                .setScale(8, RoundingMode.HALF_UP);
        BigDecimal missingBitcoinInFiat = missingBitcoin.multiply(oneBitcoinInFiat)
                .setScale(4, RoundingMode.DOWN);

        MonetaryAmount source = Money.of(missingBitcoinInFiat, fiatCurrency.getCurrencyCode());
        MonetaryAmount target = Money.of(missingBitcoin, bitcoinCurrencyUnit);

        boolean buyBitcoin = missingBitcoin.compareTo(BigDecimal.ZERO) > 0;
        if (!buyBitcoin) {
            MonetaryAmount tmp = source.abs();
            source = target.abs();
            target = tmp;
        }

        System.out.printf("📈 You should use %s to get %s%n", moneyFormat.format(source), moneyFormat.format(target));
    }
}
