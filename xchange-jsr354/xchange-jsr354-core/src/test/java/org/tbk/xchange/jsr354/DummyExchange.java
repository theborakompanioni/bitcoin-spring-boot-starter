package org.tbk.xchange.jsr354;

import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.utils.nonce.LongConstNonceFactory;
import si.mazi.rescu.SynchronizedValueFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

public class DummyExchange extends BaseExchange implements Exchange {
    private final SynchronizedValueFactory<Long> nonceFactory = new LongConstNonceFactory();

    @Override
    protected void initServices() {
        this.marketDataService = new DummyMarketDataService();
        this.tradeService = new DummyTradeService();
        this.accountService = new DummyAccountService();
    }

    @Override
    public SynchronizedValueFactory<Long> getNonceFactory() {
        return nonceFactory;
    }

    @Override
    public ExchangeSpecification getDefaultExchangeSpecification() {
        ExchangeSpecification exchangeSpecification = new ExchangeSpecification(this.getClass());
        exchangeSpecification.setSslUri("https://www.example.com");
        exchangeSpecification.setHost("www.example.com");
        exchangeSpecification.setPort(80);
        exchangeSpecification.setExchangeName("Dummy");
        exchangeSpecification.setExchangeDescription("Dummy is a exchange that should be used in application tests");
        exchangeSpecification.setShouldLoadRemoteMetaData(false);
        return exchangeSpecification;
    }

    public static class DummyMarketDataService implements MarketDataService {
        @Override
        public Ticker getTicker(CurrencyPair currencyPair, Object... args) throws IOException {
            return new Ticker.Builder()
                    .ask(new BigDecimal("0.12"))
                    .askSize(new BigDecimal("0.13"))
                    .bid(new BigDecimal("0.14"))
                    .bidSize(new BigDecimal("0.14"))
                    .instrument(currencyPair)
                    .high(new BigDecimal("0.15"))
                    .last(new BigDecimal("0.16"))
                    .low(new BigDecimal("0.17"))
                    .open(new BigDecimal("0.18"))
                    .quoteVolume(new BigDecimal("0.19"))
                    .timestamp(new Date())
                    .volume(new BigDecimal("100000"))
                    .vwap(new BigDecimal("0.2"))
                    .build();
        }
    }

    public static class DummyTradeService implements TradeService {

    }

    public static class DummyAccountService implements AccountService {

    }
}