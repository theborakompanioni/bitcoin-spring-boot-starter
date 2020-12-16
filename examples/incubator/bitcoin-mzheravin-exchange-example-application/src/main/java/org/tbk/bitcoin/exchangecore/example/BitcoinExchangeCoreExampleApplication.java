package org.tbk.bitcoin.exchangecore.example;

import exchange.core2.core.ExchangeApi;
import exchange.core2.core.common.CoreSymbolSpecification;
import exchange.core2.core.common.L2MarketData;
import exchange.core2.core.common.api.binary.BatchAddSymbolsCommand;
import exchange.core2.core.common.api.reports.TotalCurrencyBalanceReportQuery;
import exchange.core2.core.common.api.reports.TotalCurrencyBalanceReportResult;
import exchange.core2.core.common.cmd.CommandResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.context.WebServerPortFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Slf4j
@SpringBootApplication
public class BitcoinExchangeCoreExampleApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(BitcoinExchangeCoreExampleApplication.class)
                .listeners(applicationPidFileWriter(), webServerPortFileWriter())
                .web(WebApplicationType.SERVLET)
                .profiles("development", "local")
                .run(args);
    }

    public static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }

    public static ApplicationListener<?> webServerPortFileWriter() {
        return new WebServerPortFileWriter("application.port");
    }

    @Bean
    public CommandLineRunner printFeesCollected(TaskScheduler scheduler,
                                                ExchangeApi exchangeApi) {
        Runnable task = () -> {
            try {
                // check fees collected
                Future<TotalCurrencyBalanceReportResult> totalsReport = exchangeApi.processReport(new TotalCurrencyBalanceReportQuery(), 0);
                TotalCurrencyBalanceReportResult totalCurrencyBalanceReportResult = totalsReport.get();
                log.info("total currency balance report: {}", totalCurrencyBalanceReportResult);
            } catch (Exception e) {
                log.error("", e);
            }
        };

        return args -> scheduler.scheduleWithFixedDelay(task, Duration.ofSeconds(10L));
    }

    @Bean
    public CommandLineRunner exchangeCoreDemoRunner(ExchangeApi exchangeApi,
                                                    CoreSymbolSpecification symbolSpecXbtLtc) {
        return args -> {
            log.info("======================================================");
            log.info("add symbol spec: {}", symbolSpecXbtLtc);
            CompletableFuture<CommandResultCode> batchAddSymbolsFuture = exchangeApi.submitBinaryDataAsync(new BatchAddSymbolsCommand(symbolSpecXbtLtc));
            CommandResultCode batchAddSymbolsResult = batchAddSymbolsFuture.get();
            log.info("result: {}", batchAddSymbolsResult);
            log.info("======================================================");

            log.info("======================================================");
            log.info("get order book: {}", symbolSpecXbtLtc);
            CompletableFuture<L2MarketData> orderBookFuture = exchangeApi.requestOrderBookAsync(symbolSpecXbtLtc.getSymbolId(), 10);
            L2MarketData orderBookResult = orderBookFuture.get();
            log.info("result: {}", orderBookResult);
            log.info("======================================================");
        };
    }
}
