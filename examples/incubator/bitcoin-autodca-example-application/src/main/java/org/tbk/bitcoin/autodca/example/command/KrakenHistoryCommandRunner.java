package org.tbk.bitcoin.autodca.example.command;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.kraken.dto.trade.KrakenOrder;
import org.knowm.xchange.kraken.service.KrakenTradeService;
import org.springframework.boot.ApplicationArguments;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Slf4j
public class KrakenHistoryCommandRunner extends ConditionalOnNonOptionApplicationRunner {
    private final KrakenExchange exchange;

    public KrakenHistoryCommandRunner(KrakenExchange exchange) {
        super("history");
        this.exchange = requireNonNull(exchange);
    }

    @Override
    @SneakyThrows
    protected void doRun(ApplicationArguments args) {
        log.info("Show history from exchange {}", exchange);

        KrakenTradeService tradeService = (KrakenTradeService) exchange.getTradeService();

        Map<String, KrakenOrder> openOrders = tradeService.getKrakenOpenOrders();
        System.out.printf("======================================%n");
        System.out.printf("🐇 Open Orders: %d %n", openOrders.size());
        System.out.printf("--------------------------------------%n");
        openOrders.forEach((key, val) -> {
            System.out.printf("💸 %s: %-4s\t%12s\t(%5s%%)\t@ %-9s\t%8s\t%3s%n",
                    key,
                    val.getOrderDescription().getType(),
                    val.getVolume().toPlainString(),
                    val.getVolumeExecuted().multiply(BigDecimal.valueOf(100L))
                            .divide(val.getVolume(), RoundingMode.HALF_DOWN)
                            .setScale(1, RoundingMode.HALF_DOWN)
                            .toPlainString(),
                    val.getOrderDescription().getOrderType(),
                    val.getOrderDescription().getPrice().toPlainString(),
                    val.getOrderDescription().getAssetPair());
        });

        Map<String, KrakenOrder> closedOrders = tradeService.getKrakenClosedOrders();
        System.out.printf("======================================%n");
        System.out.printf("🦔 Closed Orders: %d %n", closedOrders.size());
        System.out.printf("--------------------------------------%n");
        closedOrders.forEach((key, val) -> {
            System.out.printf("💸 %s: %-8s\t%-4s\t%12s\t(%5s%%)\t@ %-9s\t%8s\t%3s%n",
                    key,
                    val.getStatus(),
                    val.getOrderDescription().getType(),
                    val.getVolume().toPlainString(),
                    val.getVolumeExecuted().multiply(BigDecimal.valueOf(100L))
                            .divide(val.getVolume(), RoundingMode.HALF_DOWN)
                            .setScale(1, RoundingMode.HALF_DOWN)
                            .toPlainString(),
                    val.getOrderDescription().getOrderType(),
                    val.getPrice().toPlainString(),
                    val.getOrderDescription().getAssetPair());
        });

    }
}
