package org.tbk.bitcoin.example.payreq.exchangerate;

import com.google.common.base.MoreObjects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.tbk.bitcoin.example.payreq.invoice.Invoice;

import javax.money.CurrencyUnit;
import javax.money.NumberValue;
import javax.money.convert.RateType;
import javax.persistence.Table;
import java.time.Instant;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Slf4j
@Getter
@Setter(AccessLevel.PACKAGE)
@Table(name = "exchange_rate")
public class ExchangeRate extends AbstractAggregateRoot<Invoice> implements AggregateRoot<ExchangeRate, ExchangeRate.ExchangeRateId> {

    private final ExchangeRateId id;

    private final long createdAt;

    private final String providerName;

    private final RateType rateType;

    private final CurrencyUnit baseCurrency;

    private final CurrencyUnit termCurrency;

    private final NumberValue factor;

    ExchangeRate(String providerName, RateType rateType, CurrencyUnit baseCurrency, CurrencyUnit termCurrency, NumberValue factor) {
        this.id = ExchangeRateId.of(UUID.randomUUID().toString());
        this.createdAt = Instant.now().toEpochMilli();
        this.providerName = requireNonNull(providerName);
        this.rateType = requireNonNull(rateType);
        this.baseCurrency = requireNonNull(baseCurrency);
        this.termCurrency = requireNonNull(termCurrency);
        this.factor = requireNonNull(factor);

        registerEvent(new ExchangeRateCreatedEvent(this));
    }

    @AfterDomainEventPublication
    void afterDomainEventPublication() {
        log.info("AfterDomainEventPublication");
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("createdAt", createdAt)
                .add("providerName", providerName)
                .add("rateType", rateType)
                .add("currencyPair", String.format("%s/%s", baseCurrency, termCurrency))
                .add("factor", factor)
                .toString();
    }

    @Value(staticConstructor = "of")
    public static class ExchangeRateId implements Identifier {
        public static ExchangeRateId create() {
            return ExchangeRateId.of(UUID.randomUUID().toString());
        }

        String id;
    }

    @Value(staticConstructor = "of")
    public static class ExchangeRateCreatedEvent {

        ExchangeRate domain;

        public String toString() {
            return "ExchangeRateCreatedEvent";
        }
    }
}
