package org.tbk.bitcoin.txstats.example.score.cryptoscamdb;

import com.google.common.base.Supplier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.tbk.bitcoin.txstats.example.score.cryptoscamdb.client.AddressesResponseDto;
import org.tbk.bitcoin.txstats.example.score.cryptoscamdb.client.CheckResponseDto;
import org.tbk.bitcoin.txstats.example.score.cryptoscamdb.client.CryptoScamDbClient;
import org.tbk.bitcoin.txstats.example.score.cryptoscamdb.client.EntryDto;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class CryptoScamDbServiceImpl implements CryptoScamDbService {

    private static final String ADDRESS_CACHE_KEY = "*";

    private final CryptoScamDbClient client;

    private final LoadingCache<String, CheckResponseDto> checkCache = CacheBuilder.newBuilder()
            .recordStats()
            .expireAfterWrite(Duration.ofMinutes(30))
            .maximumSize(10_000)
            .build(new CacheLoader<>() {
                @Override
                public CheckResponseDto load(String key) {
                    return client.check(key);
                }
            });

    private final LoadingCache<String, AddressesResponseDto> addressesCache = CacheBuilder.newBuilder()
            .recordStats()
            .refreshAfterWrite(Duration.ofMinutes(30))
            .maximumSize(1)
            .build(new CacheLoader<>() {
                @Override
                public AddressesResponseDto load(String key) {
                    checkArgument(ADDRESS_CACHE_KEY.equals(key), "'key' must be " + ADDRESS_CACHE_KEY);
                    return client.addresses();
                }
            });

    private final Supplier<AddressesResponseDto> addressesSupplier = () -> addressesCache.getUnchecked(ADDRESS_CACHE_KEY);

    public CryptoScamDbServiceImpl(CryptoScamDbClient client) {
        this.client = requireNonNull(client);
    }

    @Override
    public List<EntryDto> findMetaInfoOfAddress(String address) {
        AddressesResponseDto addressesResponseDto = addressesSupplier.get();

        Optional<List<EntryDto>> entries = Optional
                .ofNullable(addressesResponseDto.getResult()
                        .get(address));

        if (entries.isPresent()) {
            return entries.get();
        }

        CheckResponseDto checkResponse = checkCache.getUnchecked(address);
        if (!checkResponse.isSuccess()) {
            return Collections.emptyList();
        }

        return checkResponse.getResult().getEntries();
    }


}
