package org.tbk.bitcoin.txstats.example.cache;

import lombok.Builder;
import lombok.NonNull;
import org.tbk.bitcoin.jsonrpc.cache.*;

import static java.util.Objects.requireNonNull;

public final class AppCacheFacade {

    @NonNull
    private final CacheFacade bitcoinJsonRpcCacheFacade;

    @NonNull
    private final CurrencyConversionCache currencyConversionCache;

    @Builder
    private AppCacheFacade(CacheFacade bitcoinJsonRpcCacheFacade, CurrencyConversionCache currencyConversionCache) {
        this.bitcoinJsonRpcCacheFacade = requireNonNull(bitcoinJsonRpcCacheFacade);
        this.currencyConversionCache = requireNonNull(currencyConversionCache);
    }

    public CurrencyConversionCache currencyConversion() {
        return currencyConversionCache;
    }

    protected final CacheFacade delegate() {
        return bitcoinJsonRpcCacheFacade;
    }

    public TransactionCache tx() {
        return delegate().tx();
    }

    public RawTransactionInfoCache txInfo() {
        return delegate().txInfo();
    }

    public BlockCache block() {
        return delegate().block();
    }

    public BlockInfoCache blockInfo() {
        return delegate().blockInfo();
    }
}
