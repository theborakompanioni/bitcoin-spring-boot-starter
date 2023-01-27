package org.tbk.bitcoin.autodca.example.command;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.kraken.dto.trade.KrakenOrderResponse;
import org.knowm.xchange.kraken.dto.trade.KrakenStandardOrder;
import org.knowm.xchange.kraken.dto.trade.KrakenStandardOrder.KrakenOrderBuilder;
import org.knowm.xchange.kraken.dto.trade.KrakenType;
import org.knowm.xchange.kraken.service.KrakenAccountService;
import org.knowm.xchange.kraken.service.KrakenMarketDataService;
import org.knowm.xchange.kraken.service.KrakenTradeService;
import org.springframework.boot.ApplicationArguments;
import org.tbk.bitcoin.autodca.example.BitcoinAutoDcaExampleApplicationConfig.DryRunOption;
import org.tbk.bitcoin.autodca.example.BitcoinAutoDcaExampleProperties;

import javax.money.Monetary;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
public class KrakenStackCommandRunner extends ConditionalOnNonOptionApplicationRunner {
    private static final int BTC_FRACTION_DIGITS = Monetary.getCurrency("BTC").getDefaultFractionDigits();

    private final KrakenExchange exchange;
    private final BitcoinAutoDcaExampleProperties properties;
    private final DryRunOption dryRun;

    public KrakenStackCommandRunner(KrakenExchange exchange,
                                    BitcoinAutoDcaExampleProperties properties,
                                    DryRunOption dryRun) {
        super("stack");
        this.exchange = requireNonNull(exchange);
        this.properties = requireNonNull(properties);
        this.dryRun = requireNonNull(dryRun);
    }

    @Override
    @SneakyThrows
    protected void doRun(ApplicationArguments args) {
        log.info("Would stack on exchange {}", exchange);

        Currency bitcoin = Currency.BTC;

        Currency fiatCurrency = Currency.getInstance(properties.getFiatCurrency());
        BigDecimal fiatAmount = properties.getFiatAmount();

        CurrencyPair currencyPair = new CurrencyPair(bitcoin, fiatCurrency);
        boolean supportedCurrencyPair = exchange.getExchangeInstruments().contains(currencyPair);
        if (!supportedCurrencyPair) {
            throw new IllegalStateException("Currency pair is not supported: " + currencyPair);
        }

        // ---------------------------------------------- balance
        KrakenAccountService accountService = (KrakenAccountService) exchange.getAccountService();
        Map<String, BigDecimal> krakenBalance = accountService.getKrakenBalance();

        BigDecimal fiatBalance = Optional.ofNullable(krakenBalance.get("Z" + fiatCurrency.getCurrencyCode()))
                .or(() -> Optional.ofNullable(krakenBalance.get(fiatCurrency.getCurrencyCode())))
                .orElse(BigDecimal.ZERO);

        BigDecimal bitcoinBalance = bitcoin.getCurrencyCodes().stream()
                .filter(it -> krakenBalance.containsKey("X" + it))
                .map(it -> krakenBalance.get("X" + it))
                .findFirst()
                .or(() -> Optional.ofNullable(krakenBalance.get(bitcoin.getCurrencyCode())))
                .orElse(BigDecimal.ZERO);

        System.out.printf("💰 Balance: %s %s / %s %s%n",
                fiatBalance.toPlainString(), fiatCurrency,
                bitcoinBalance.toPlainString(), bitcoin);

        if (fiatBalance.compareTo(fiatAmount) < 0) {
            System.out.printf("❌ Balance is too low - balance < order price: %s < %s%n",
                    fiatBalance.toPlainString(), fiatAmount.toPlainString());
            return;
        }
        // ---------------------------------------------- balance - end

        // ---------------------------------------------- ask/bid
        KrakenMarketDataService marketDataService = (KrakenMarketDataService) exchange.getMarketDataService();
        Ticker ticker = marketDataService.getTicker(currencyPair);

        System.out.printf("📈 Ask: %s %s%n", ticker.getAsk(), fiatCurrency);
        System.out.printf("📉 Bid: %s %s%n", ticker.getBid(), fiatCurrency);
        // ---------------------------------------------- ask/bid - end

        // run dry test with massively undervalued order! from the kraken docs:
        // "[...] we recommend placing very small market orders (orders for the minimum order size),
        // or limit orders that are priced far away from the current market price"
        BigDecimal priceMultiplier = dryRun.isEnabled() ? new BigDecimal("0.1") : BigDecimal.ONE;

        BigDecimal buyingPrice = ticker.getBid().multiply(priceMultiplier);
        BigDecimal cryptoAmount = fiatAmount.setScale(BTC_FRACTION_DIGITS, RoundingMode.HALF_DOWN)
                .divide(buyingPrice, RoundingMode.HALF_DOWN);

        // ---------------------------------------------- place limit order
        boolean validateOnly = dryRun.isEnabled();

        KrakenOrderBuilder krakenLimitOrderBuilder = KrakenStandardOrder.getLimitOrderBuilder(
                        currencyPair,
                        KrakenType.BUY,
                        buyingPrice.toPlainString(),
                        cryptoAmount)
                .withValidateOnly(validateOnly);

        KrakenStandardOrder buyLimitOrder = krakenLimitOrderBuilder.buildOrder();

        KrakenTradeService tradeService = (KrakenTradeService) exchange.getTradeService();
        System.out.printf("💸 Order: %s %s %s @ limit %s %s%n",
                buyLimitOrder.getType(),
                buyLimitOrder.getVolume(),
                buyLimitOrder.getAssetPair().base,
                buyLimitOrder.getPrice(),
                buyLimitOrder.getAssetPair().counter);

        if (dryRun.isEnabled()) {
            System.out.printf("📎 Fake (dry-run) Transaction IDs: %s%n", Arrays.asList("42", "1337"));
        } else {
            KrakenOrderResponse krakenOrderResponse = tradeService.placeKrakenOrder(buyLimitOrder);
            System.out.printf("📎 Transaction IDs: %s%n", krakenOrderResponse.getTransactionIds());
        }
        // ---------------------------------------------- place limit - end
    }
}
