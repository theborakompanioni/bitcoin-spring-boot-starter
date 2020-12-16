package org.tbk.bitcoin.exchangecore.example;

import exchange.core2.core.ExchangeApi;
import exchange.core2.core.ExchangeCore;
import exchange.core2.core.IEventsHandler;
import exchange.core2.core.SimpleEventsProcessor;
import exchange.core2.core.common.CoreSymbolSpecification;
import exchange.core2.core.common.SymbolType;
import exchange.core2.core.common.config.ExchangeConfiguration;
import exchange.core2.core.common.config.LoggingConfiguration;
import exchange.core2.core.common.config.SerializationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.EnumSet;

@Slf4j
@Configuration
@EnableScheduling
public class BitcoinExchangeCoreExampleApplicationConfig {

    @Bean
    public CoreSymbolSpecification symbolSpecXbtLtc() {

        // currency code constants
        final int currencyCodeXbt = 11;
        final int currencyCodeLtc = 15;

        // symbol constants
        final int symbolXbtLtc = 241;

        // create symbol specification and publish it
        CoreSymbolSpecification symbolSpecXbtLtc = CoreSymbolSpecification.builder()
                .symbolId(symbolXbtLtc)         // symbol id
                .type(SymbolType.CURRENCY_EXCHANGE_PAIR)
                .baseCurrency(currencyCodeXbt)    // base = satoshi (1E-8)
                .quoteCurrency(currencyCodeLtc)   // quote = litoshi (1E-8)
                .baseScaleK(1_000_000L) // 1 lot = 1M satoshi (0.01 BTC)
                .quoteScaleK(10_000L)   // 1 price step = 10K litoshi
                .takerFee(1900L)        // taker fee 1900 litoshi per 1 lot
                .makerFee(700L)         // maker fee 700 litoshi per 1 lot
                .build();

        return symbolSpecXbtLtc;
    }

    @Bean
    public ExchangeApi exchangeApi(ExchangeCore exchangeCore) {
        // get exchange API for publishing commands
        ExchangeApi api = exchangeCore.getApi();

        return api;
    }

    @Bean
    public ExchangeCore exchangeCore(SimpleEventsProcessor eventsProcessor,
                                     ExchangeConfiguration exchangeConfiguration) {
        ExchangeCore exchangeCore = ExchangeCore.builder()
                .resultsConsumer(eventsProcessor)
                .exchangeConfiguration(exchangeConfiguration)
                .build();

        exchangeCore.startup();

        return exchangeCore;
    }

    @Bean
    public DisposableBean exchangeCoreShutdown(ExchangeCore exchangeCore) {
        return () -> {
            log.info("terminating exchange-core..");
            exchangeCore.shutdown();
            log.info("terminated exchange-core.");
        };
    }

    @Bean
    public ExchangeConfiguration exchangeConfiguration(SerializationConfiguration serializationConfiguration,
                                                       LoggingConfiguration loggingConfiguration) {
        ExchangeConfiguration conf = ExchangeConfiguration.defaultBuilder()
                .serializationCfg(serializationConfiguration)
                .loggingCfg(loggingConfiguration)
                .build();
        return conf;
    }

    @Bean
    public SerializationConfiguration serializationConfiguration() {
        //return SerializationConfiguration.DISK_JOURNALING;
        return SerializationConfiguration.DEFAULT;
    }

    @Bean
    public LoggingConfiguration loggingConfiguration() {
        return LoggingConfiguration.builder()
                .loggingLevels(EnumSet.allOf(LoggingConfiguration.LoggingLevel.class))
                .build();
    }

    @Bean
    public SimpleEventsProcessor eventsProcessor() {
        // simple async events handler
        SimpleEventsProcessor eventsProcessor = new SimpleEventsProcessor(new IEventsHandler() {
            @Override
            public void tradeEvent(TradeEvent tradeEvent) {
                log.info("Trade event: {}", tradeEvent);
            }

            @Override
            public void reduceEvent(ReduceEvent reduceEvent) {
                log.info("Reduce event: {}", reduceEvent);
            }

            @Override
            public void rejectEvent(RejectEvent rejectEvent) {
                log.info("Reject event: {}", rejectEvent);
            }

            @Override
            public void commandResult(ApiCommandResult commandResult) {
                log.info("Command result: {}", commandResult);
            }

            @Override
            public void orderBook(OrderBook orderBook) {
                log.info("OrderBook event: {}", orderBook);
            }
        });
        return eventsProcessor;
    }
}
