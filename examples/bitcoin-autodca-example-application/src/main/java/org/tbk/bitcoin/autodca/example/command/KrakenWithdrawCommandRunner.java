package org.tbk.bitcoin.autodca.example.command;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.kraken.dto.account.WithdrawInfo;
import org.knowm.xchange.kraken.service.KrakenAccountService;
import org.springframework.boot.ApplicationArguments;
import org.tbk.bitcoin.autodca.example.BitcoinAutoDcaExampleApplicationConfig.DryRunOption;
import org.tbk.bitcoin.autodca.example.BitcoinAutoDcaExampleProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
public class KrakenWithdrawCommandRunner extends ConditionalOnNonOptionApplicationRunner {
    private final KrakenExchange exchange;
    private final BitcoinAutoDcaExampleProperties properties;
    private final DryRunOption dryRun;

    public KrakenWithdrawCommandRunner(KrakenExchange exchange,
                                       BitcoinAutoDcaExampleProperties properties,
                                       DryRunOption dryRun) {
        super("withdraw");
        this.exchange = requireNonNull(exchange);
        this.properties = requireNonNull(properties);
        this.dryRun = requireNonNull(dryRun);
    }

    @Override
    @SneakyThrows
    protected void doRun(ApplicationArguments args) {
        log.info("Would withdraw from exchange {}", exchange);

        BigDecimal maxRelativeFee = this.properties.getMaxRelativeFee()
                .orElseThrow(() -> new IllegalStateException("Max relative fee is not provided"));

        KrakenAccountService accountService = (KrakenAccountService) exchange.getAccountService();

        String asset = "XBT";
        String address = properties.getWithdrawAddress();
        WithdrawInfo withdrawInfo = accountService.getWithdrawInfo(null, asset, address, BigDecimal.ZERO);

        BigDecimal relativeFee = BigDecimal.ONE.setScale(8, RoundingMode.HALF_UP)
                .divide(withdrawInfo.getLimit(), RoundingMode.HALF_UP)
                .multiply(new BigDecimal(withdrawInfo.getFee()));

        BigDecimal displayFee = relativeFee.multiply(BigDecimal.valueOf(100L))
                .setScale(2, RoundingMode.HALF_UP);

        System.out.printf("💡 Relative fee of withdrawal amount: %s%n", displayFee.toPlainString());

        boolean feeAssertionSatisfied = relativeFee
                .compareTo(maxRelativeFee.divide(BigDecimal.valueOf(100L), RoundingMode.HALF_DOWN)) < 0;

        // Place withdrawal when fee is low enough (relatively)
        if (!feeAssertionSatisfied) {
            System.out.printf("❌ Fee is too high - rel fee > max rel fee: %s > %s%n",
                    displayFee.toPlainString(),
                    maxRelativeFee.multiply(BigDecimal.valueOf(100L)).toPlainString());
            return;
        }

        System.out.printf("💰 Withdraw %s %s now.%n",
                withdrawInfo.getLimit().toPlainString(),
                asset);

        if (dryRun.isEnabled()) {
            System.out.printf("💰 Fake (dry-run) Withdrawal reference ID: %s%n", "1337");
        } else {
            String withdrawRefIdOrNull = accountService.withdrawFunds(Currency.BTC, relativeFee, address);
            String displayWithdrawRefId = Optional.ofNullable(withdrawRefIdOrNull).orElse("<empty>");

            System.out.printf("💰 Withdrawal reference ID: %s%n", displayWithdrawRefId);
        }
    }
}
