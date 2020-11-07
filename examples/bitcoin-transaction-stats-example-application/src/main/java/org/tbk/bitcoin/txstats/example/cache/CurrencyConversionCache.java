package org.tbk.bitcoin.txstats.example.cache;

import com.google.common.cache.ForwardingLoadingCache;
import com.google.common.cache.LoadingCache;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;

import javax.money.convert.ConversionQuery;
import javax.money.convert.CurrencyConversion;

public final class CurrencyConversionCache extends ForwardingLoadingCache.SimpleForwardingLoadingCache<ConversionQuery, CurrencyConversion> {
    public CurrencyConversionCache(LoadingCache<ConversionQuery, CurrencyConversion> delegate) {
        super(delegate);
    }
}
