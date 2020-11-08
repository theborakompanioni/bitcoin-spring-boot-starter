package org.tbk.bitcoin.txstats.example.cache;

import lombok.Builder;
import lombok.NonNull;

@Builder
public class CacheFacade {
    @NonNull
    private final TransactionCache transactionCache;

    @NonNull
    private final RawTransactionInfoCache rawTransactionInfoCache;

    @NonNull
    private final BlockInfoCache blockInfoCache;

    @NonNull
    private final CurrencyConversionCache currencyConversionCache;

    public TransactionCache tx() {
        return transactionCache;
    }

    public RawTransactionInfoCache txInfo() {
        return rawTransactionInfoCache;
    }

    public BlockInfoCache blockInfo() {
        return blockInfoCache;
    }

    public CurrencyConversionCache currencyConversion() {
        return currencyConversionCache;
    }
}
