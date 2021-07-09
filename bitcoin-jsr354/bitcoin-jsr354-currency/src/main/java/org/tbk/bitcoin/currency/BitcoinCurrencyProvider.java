/*
 * Copyright (c) 2013, 2015, Werner Keil and others by the @author tag.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tbk.bitcoin.currency;

import javax.money.CurrencyContext;
import javax.money.CurrencyContextBuilder;
import javax.money.CurrencyQuery;
import javax.money.CurrencyUnit;
import javax.money.spi.CurrencyProviderSpi;
import java.util.Collections;
import java.util.Set;

/**
 * A BitcoinCurrencyProvider based on work in the javamoney-shelter.
 *
 * @author Sean Gilligan
 * @author Werner Keil
 */
public final class BitcoinCurrencyProvider implements CurrencyProviderSpi {
    private static final String PROVIDER_NAME = "BitcoinCurrencyProvider";

    private static final CurrencyContext CONTEXT = CurrencyContextBuilder.of(PROVIDER_NAME)
            .build();

    private static final CurrencyUnit bitcoinUnit = new BitcoinCurrencyUnit(CONTEXT);

    private static final Set<CurrencyUnit> bitcoinUnitSet = Collections.singleton(bitcoinUnit);

    public static String providerName() {
        return PROVIDER_NAME;
    }

    /**
     * Return a {@link CurrencyUnit} instances matching the given
     * {@link javax.money.CurrencyQuery}.
     *
     * @param query the {@link javax.money.CurrencyQuery} containing the parameters determining the query. not null.
     * @return the corresponding {@link CurrencyUnit}s matching, never null.
     */
    @Override
    public Set<CurrencyUnit> getCurrencies(CurrencyQuery query) {
        // Query for currencyCode BTC or default query returns bitcoinSet else emptySet.
        return (query.getCurrencyCodes().contains(bitcoinUnit.getCurrencyCode())
                || query.getCurrencyCodes().isEmpty()) ? bitcoinUnitSet : Collections.emptySet();
    }

}