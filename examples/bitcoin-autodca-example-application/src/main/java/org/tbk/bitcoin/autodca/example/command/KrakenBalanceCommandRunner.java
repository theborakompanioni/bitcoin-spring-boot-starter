package org.tbk.bitcoin.autodca.example.command;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.kraken.service.KrakenAccountService;
import org.springframework.boot.ApplicationArguments;

import java.math.BigDecimal;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Slf4j
public class KrakenBalanceCommandRunner extends ConditionalOnNonOptionApplicationRunner {

    private final KrakenExchange exchange;

    public KrakenBalanceCommandRunner(KrakenExchange exchange) {
        super("balance");
        this.exchange = requireNonNull(exchange);
    }

    @Override
    @SneakyThrows
    protected void doRun(ApplicationArguments args) {
        log.debug("Fetch balance on exchange {}", exchange);

        KrakenAccountService accountService = (KrakenAccountService) exchange.getAccountService();
        Map<String, BigDecimal> krakenBalance = accountService.getKrakenBalance();

        krakenBalance.forEach((currencyCode, balance) -> {
            System.out.printf("💰 %s: %s%n", currencyCode, balance.toPlainString());
        });
    }
}
