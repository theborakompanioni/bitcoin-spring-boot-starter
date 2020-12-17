package org.tbk.xchange.jsr354;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;

import javax.money.convert.ProviderContextBuilder;
import javax.money.convert.RateType;
import java.util.Optional;

public final class MoreProviderContexts {

    private MoreProviderContexts() {
        throw new UnsupportedOperationException();
    }

    /**
     * A factory method creating a simple provider context.
     * <p>
     * @implNote This method is primarily intended for internal usage.
     *   Consider building your own context with {@link ProviderContextBuilder}.
     *
     * @param exchange the exchange to build the context from
     * @return a simple provider context with basic information from the given exchange
     */
    public static ProviderContextBuilder createSimpleProviderContextBuilder(Exchange exchange) {
        String providerClassName = exchange.getClass().getSimpleName();
        String providerId = providerClassName
                .replace("Exchange", "")
                .toUpperCase();

        ExchangeSpecification specification = exchange.getDefaultExchangeSpecification();
        String exchangeName = Optional.ofNullable(specification.getExchangeName()).orElse(providerClassName);
        String exchangeDescription = Optional.ofNullable(specification.getExchangeDescription()).orElse("");

        return ProviderContextBuilder.of(providerId, RateType.DEFERRED)
                .set("providerName", exchangeName)
                .set("providerDescription", exchangeDescription);
    }
}
