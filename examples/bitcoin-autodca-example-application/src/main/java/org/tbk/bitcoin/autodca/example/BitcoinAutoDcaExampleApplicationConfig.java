package org.tbk.bitcoin.autodca.example;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.kraken.KrakenExchange;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.tbk.bitcoin.autodca.example.command.*;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BitcoinAutoDcaExampleProperties.class)
public class BitcoinAutoDcaExampleApplicationConfig {

    @Value
    public static class DryRunOption {
        boolean enabled;
    }

    private final BitcoinAutoDcaExampleProperties properties;

    public BitcoinAutoDcaExampleApplicationConfig(BitcoinAutoDcaExampleProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    DryRunOption dryRunOption(Environment env, ApplicationArguments applicationArguments) {
        boolean isProductionEnvironment = env.acceptsProfiles(Profiles.of("production"));
        boolean isExplicitTestEnvironment = env.acceptsProfiles(Profiles.of("test | debug | development | staging"));
        boolean isExplicitDryRunGivenByUserViaArguments = applicationArguments.containsOption("dry");
        boolean isExplicitDryRunGivenByUserViaProperties = properties.getDry();

        boolean dryRunEnabled = !isProductionEnvironment
                || isExplicitTestEnvironment
                || isExplicitDryRunGivenByUserViaArguments
                || isExplicitDryRunGivenByUserViaProperties;

        return new DryRunOption(dryRunEnabled);
    }

    @Bean
    ApplicationRunner dryRunOptionLogger(DryRunOption dryRunOption) {
        return args -> {
            if (dryRunOption.isEnabled()) {
                log.info("================================");
                log.info("= THIS IS JUST A VALIDATION RUN!");
                log.info("================================");
            }
        };
    }

    @Bean
    @ConditionalOnNotWebApplication
    ApplicationRunner versionCommandRunner(BuildProperties buildProperties) {
        return new VersionCommandRunner(buildProperties);
    }

    @Bean
    @ConditionalOnNotWebApplication
    ApplicationRunner helpCommandRunner(BuildProperties buildProperties) {
        return new HelpCommandRunner(buildProperties);
    }

    @Bean
    @ConditionalOnNotWebApplication
    ApplicationRunner stackCommandRunner(Exchange exchange, DryRunOption dryRunOption) {
        KrakenExchange krakenExchange = toKrakenExchangeOrThrow(exchange);
        return new KrakenStackCommandRunner(krakenExchange, this.properties, dryRunOption);
    }

    @Bean
    @ConditionalOnNotWebApplication
    ApplicationRunner withdrawCommandRunner(Exchange exchange, DryRunOption dryRunOption) {
        KrakenExchange krakenExchange = toKrakenExchangeOrThrow(exchange);
        return new KrakenWithdrawCommandRunner(krakenExchange, this.properties, dryRunOption);
    }

    @Bean
    @ConditionalOnNotWebApplication
    ApplicationRunner balanceCommandRunner(Exchange exchange) {
        KrakenExchange krakenExchange = toKrakenExchangeOrThrow(exchange);
        return new KrakenBalanceCommandRunner(krakenExchange);
    }

    @Bean
    @ConditionalOnNotWebApplication
    ApplicationRunner tickerCommandRunner(Exchange exchange) {
        return new TickerCommandRunner(exchange, this.properties);
    }

    @Bean
    @ConditionalOnNotWebApplication
    ApplicationRunner historyCommandRunner(Exchange exchange) {
        KrakenExchange krakenExchange = toKrakenExchangeOrThrow(exchange);
        return new KrakenHistoryCommandRunner(krakenExchange);
    }

    @Bean
    @ConditionalOnNotWebApplication
    ApplicationRunner rebalanceCommandRunner(Exchange exchange, DryRunOption dryRunOption) {
        KrakenExchange krakenExchange = toKrakenExchangeOrThrow(exchange);
        return new KrakenRebalanceCommandRunner(krakenExchange, this.properties, dryRunOption);
    }

    private KrakenExchange toKrakenExchangeOrThrow(Exchange exchange) {
        boolean isKrakenExchange = exchange instanceof KrakenExchange;

        if (!isKrakenExchange) {
            String errorMessage = String.format("Unsupported exchange %s: Only Kraken is currently supported",
                    exchange.getExchangeSpecification().getExchangeName());
            throw new IllegalStateException(errorMessage);
        }

        return (KrakenExchange) exchange;
    }
}
