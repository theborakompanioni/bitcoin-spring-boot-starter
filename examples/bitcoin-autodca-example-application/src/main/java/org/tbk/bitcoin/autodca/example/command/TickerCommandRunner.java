package org.tbk.bitcoin.autodca.example.command;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.params.CurrencyPairsParam;
import org.springframework.boot.ApplicationArguments;
import org.tbk.bitcoin.autodca.example.BitcoinAutoDcaExampleProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
public class TickerCommandRunner extends ConditionalOnNonOptionApplicationRunner {

    private final Exchange exchange;
    private final BitcoinAutoDcaExampleProperties properties;

    public TickerCommandRunner( Exchange exchange,
                               BitcoinAutoDcaExampleProperties properties) {
        super("ticker");
        this.exchange = requireNonNull(exchange);
        this.properties = requireNonNull(properties);
    }

    @Override
    @SneakyThrows
    protected void doRun(ApplicationArguments args) {
        log.debug("Fetch ticker on exchange {}", exchange);

        Currency cryptoCurrency = Currency.BTC;

        Currency fiatCurrency = Currency.getInstance(properties.getFiatCurrency());
        CurrencyPair currencyPair = new CurrencyPair(cryptoCurrency, fiatCurrency);

        boolean supportedCurrencyPair = exchange.getExchangeSymbols().contains(currencyPair);
        if (!supportedCurrencyPair) {
            throw new IllegalStateException("Currency pair is not supported: " + currencyPair);
        }

        CurrencyPairsParam currencyPairsParam = () -> Collections.singletonList(currencyPair);
        List<Ticker> tickers = exchange.getMarketDataService().getTickers(currencyPairsParam);

        tickers.forEach(ticker -> {
            BigDecimal spreadAskBid = calcSpreadToAskPrice(ticker, ticker.getBid());
            BigDecimal spreadAskHigh = calcSpreadToAskPrice(ticker, ticker.getHigh());
            BigDecimal spreadAskLow = calcSpreadToAskPrice(ticker, ticker.getLow());
            BigDecimal spreadAskOpen = calcSpreadToAskPrice(ticker, ticker.getOpen());
            BigDecimal spreadAskLast = calcSpreadToAskPrice(ticker, ticker.getLast());

            System.out.printf("Name     \t%12s\t%10s\t%10s %n", "Value", "Instrument", "Spread");
            System.out.printf("📈 Ask:  \t%12s\t%10s%n", displayValue(ticker.getAsk()), ticker.getInstrument());
            System.out.printf("📉 Bid:  \t%12s\t%10s\t%10s%%%n", displayValue(ticker.getBid()), ticker.getInstrument(), spreadAskBid.toPlainString());
            System.out.printf("- High:  \t%12s\t%10s\t%10s%%%n", displayValue(ticker.getHigh()), ticker.getInstrument(), spreadAskHigh.toPlainString());
            System.out.printf("- Low:   \t%12s\t%10s\t%10s%%%n", displayValue(ticker.getLow()), ticker.getInstrument(), spreadAskLow.toPlainString());
            System.out.printf("- Open:  \t%12s\t%10s\t%10s%%%n", displayValue(ticker.getOpen()), ticker.getInstrument(), spreadAskOpen.toPlainString());
            System.out.printf("- Last:  \t%12s\t%10s\t%10s%%%n", displayValue(ticker.getLast()), ticker.getInstrument(), spreadAskLast.toPlainString());
        });
    }

    private BigDecimal calcSpreadToAskPrice(Ticker ticker, BigDecimal val) {
        return ticker.getAsk().subtract(val)
                .multiply(BigDecimal.valueOf(100L))
                .divide(ticker.getAsk(), RoundingMode.HALF_DOWN);
    }

    private String displayValue(BigDecimal val) {
        return val.setScale(2, RoundingMode.HALF_DOWN).toPlainString();
    }
}
