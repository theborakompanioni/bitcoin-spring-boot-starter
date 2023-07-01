package org.tbk.bitcoin.jsonrpc.config;

import com.google.common.base.Suppliers;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import io.micrometer.common.lang.NonNullApi;
import io.micrometer.common.lang.NonNullFields;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.cache.GuavaCacheMetrics;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.NetworkParameters;
import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.json.pojo.NetworkInfo;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.jsonrpc.cache.CacheFacade;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "org.tbk.bitcoin.jsonrpc.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({
        MeterBinder.class,
        BitcoinClient.class
})
@AutoConfigureAfter({
        BitcoinJsonRpcClientAutoConfiguration.class,
        BitcoinJsonRpcCacheAutoConfiguration.class
})
public class BitcoinJsonRpcMetricsConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(CacheFacade.class)
    public static class BitcoinJsonRpcCacheMetricsConfiguration {

        @Bean
        @ConditionalOnBean(CacheFacade.class)
        MeterBinder bitcoinJsonRpcCacheMetrics(CacheFacade cache) {
            return (registry) -> {
                GuavaCacheMetrics.monitor(registry, cache.block(), "block", Collections.emptyList());
                GuavaCacheMetrics.monitor(registry, cache.blockInfo(), "blockInfo", Collections.emptyList());
                GuavaCacheMetrics.monitor(registry, cache.tx(), "tx", Collections.emptyList());
                GuavaCacheMetrics.monitor(registry, cache.txInfo(), "txInfo", Collections.emptyList());
            };
        }

    }


    @Bean
    @ConditionalOnBean(BitcoinClient.class)
    BitcoinJsonRpcClientMetrics bitcoinJsonRpcClientMetrics(BitcoinClient client) {
        return new BitcoinJsonRpcClientMetrics(client);
    }

    @Slf4j
    @NonNullApi
    @NonNullFields
    public static class BitcoinJsonRpcClientMetrics implements MeterBinder {
        private static final Function<Object, Optional<String>> tryParseString = (obj) -> Optional.ofNullable(obj)
                .map(Object::toString);
        private static final Function<Object, Optional<Double>> tryParseDouble = (obj) -> tryParseString.andThen(it -> it
                        .map(Doubles::tryParse))
                .apply(obj);
        private static final Function<Object, Optional<Long>> tryParseLong = (obj) -> tryParseString.andThen(it -> it
                        .map(Longs::tryParse))
                .apply(obj);

        private final BitcoinClient client;
        private final Iterable<Tag> tags;
        private final String network;

        private final Supplier<Optional<BlockChainInfo>> blockChainInfoSupplier = Suppliers
                .memoizeWithExpiration(this::fetchBlockChainInfo, 1, TimeUnit.SECONDS);

        private final Supplier<Optional<NetworkInfo>> networkInfoSupplier = Suppliers
                .memoizeWithExpiration(this::fetchNetworkInfo, 1, TimeUnit.SECONDS);

        private final Supplier<Optional<Map<String, Object>>> mempoolInfoSupplier = Suppliers
                .memoizeWithExpiration(this::fetchMempoolInfo, 1, TimeUnit.SECONDS);

        private final Supplier<Optional<Map<String, Object>>> memoryInfoSupplier = Suppliers
                .memoizeWithExpiration(this::fetchMemoryInfo, 1, TimeUnit.SECONDS);

        public BitcoinJsonRpcClientMetrics(BitcoinClient client) {
            this(client, Tags.empty());
        }

        public BitcoinJsonRpcClientMetrics(BitcoinClient client, Iterable<Tag> tags) {
            this.client = requireNonNull(client);
            this.tags = requireNonNull(tags);

            this.network = Optional.of(client.getNetParams())
                    .map(NetworkParameters::getId)
                    .orElse("unknown");
        }

        @Override
        public void bindTo(MeterRegistry registry) {
            registerBlockchainInfo(registry);
            registerNetworkInfo(registry);
            registerMempoolInfo(registry);
            registerMemoryInfo(registry);
        }

        private Optional<BlockChainInfo> fetchBlockChainInfo() {
            try {
                return Optional.of(client.getBlockChainInfo());
            } catch (IOException e) {
                log.warn("Error while fetching 'blockchaininfo' from bitcoin jsonrpc client: {}", e.getMessage());
                return Optional.empty();
            }
        }

        private Optional<NetworkInfo> fetchNetworkInfo() {
            try {
                return Optional.of(client.getNetworkInfo());
            } catch (IOException e) {
                log.warn("Error while fetching 'networkinfo' from bitcoin jsonrpc client: {}", e.getMessage());
                return Optional.empty();
            }
        }

        /**
         * Returns a map containing mempool info. e.g.
         * {
         * "size": xxxxx,               (numeric) Current tx count
         * "bytes": xxxxx,              (numeric) Sum of all virtual transaction sizes as defined in BIP 141. Differs from actual serialized size because witness data is discounted
         * "usage": xxxxx,              (numeric) Total memory usage for the mempool
         * "maxmempool": xxxxx,         (numeric) Maximum memory usage for the mempool
         * "mempoolminfee": xxxxx       (numeric) Minimum fee rate in BTC/kB for tx to be accepted. Is the maximum of minrelaytxfee and minimum mempool fee
         * "minrelaytxfee": xxxxx       (numeric) Current minimum relay fee for transactions
         * }
         */
        private Optional<Map<String, Object>> fetchMempoolInfo() {
            try {
                return Optional.of(client.send("getmempoolinfo"));
            } catch (IOException e) {
                log.warn("Error while fetching 'mempoolinfo' from bitcoin jsonrpc client: {}", e.getMessage());
                return Optional.empty();
            }
        }

        /**
         * {
         * "used": xxxxx,          (numeric) Number of bytes used
         * "free": xxxxx,          (numeric) Number of bytes available in current arenas
         * "total": xxxxxxx,       (numeric) Total number of bytes managed
         * "locked": xxxxxx,       (numeric) Amount of bytes that succeeded locking. If this number is smaller than total, locking pages failed at some point and key data could be swapped to disk.
         * "chunks_used": xxxxx,   (numeric) Number allocated chunks
         * "chunks_free": xxxxx,   (numeric) Number unused chunks
         * }
         */
        @SuppressWarnings("unchecked")
        private Optional<Map<String, Object>> fetchMemoryInfo() {
            try {
                Map<String, Object> memoryInfo = client.send("getmemoryinfo");
                return Optional.ofNullable(memoryInfo)
                        .map(it -> it.get("locked"))
                        .map(it -> (Map<String, Object>) it);
            } catch (IOException e) {
                log.warn("Error while fetching 'getmemoryinfo' from bitcoin jsonrpc client: {}", e.getMessage());
                return Optional.empty();
            }
        }

        private void registerBlockchainInfo(MeterRegistry registry) {
            Gauge.builder("bitcoin.blockchain.blocks", client, client -> blockChainInfoSupplier.get()
                            .map(BlockChainInfo::getBlocks)
                            .orElse(-1))
                    .tags(tags).tag("network", this.network)
                    .description("Number of all blocks in the chain")
                    .register(registry);

            Gauge.builder("bitcoin.blockchain.headers", client, client -> blockChainInfoSupplier.get()
                            .map(BlockChainInfo::getHeaders)
                            .orElse(-1))
                    .tags(tags).tag("network", this.network)
                    .description("Number of all block headers")
                    .register(registry);

            Gauge.builder("bitcoin.blockchain.difficulty", client, client -> blockChainInfoSupplier.get()
                            .map(BlockChainInfo::getDifficulty)
                            .map(BigDecimal::doubleValue)
                            .orElse(-1d))
                    .tags(tags).tag("network", this.network)
                    .description("Number representing the proof-of-work difficulty")
                    .register(registry);

            Gauge.builder("bitcoin.blockchain.verification.progress", client, client -> blockChainInfoSupplier.get()
                            .map(BlockChainInfo::getVerificationProgress)
                            .map(BigDecimal::doubleValue)
                            .orElse(-1d))
                    .tags(tags).tag("network", this.network)
                    .description("Number representing an estimate of the verification progress [0..1]")
                    .register(registry);
        }

        private void registerNetworkInfo(MeterRegistry registry) {
            Gauge.builder("bitcoin.network.connections", client, client -> networkInfoSupplier.get()
                            .map(NetworkInfo::getConnections)
                            .orElse(-1))
                    .tags(tags).tag("network", this.network)
                    .description("Number of connections")
                    .register(registry);

            Gauge.builder("bitcoin.network.timeoffset", client, client -> networkInfoSupplier.get()
                            .map(NetworkInfo::getTimeOffset)
                            .orElse(-1))
                    .tags(tags).tag("network", this.network)
                    .description("The time offset")
                    .register(registry);

            Gauge.builder("bitcoin.network.version", client, client -> networkInfoSupplier.get()
                            .map(NetworkInfo::getVersion)
                            .orElse(-1))
                    .tags(tags).tag("network", this.network)
                    .description("The server version")
                    .register(registry);
        }

        private void registerMempoolInfo(MeterRegistry registry) {
            Gauge.builder("bitcoin.mempool.size", client, client -> mempoolInfoSupplier.get()
                            .map(it -> it.get("size"))
                            .flatMap(tryParseLong)
                            .orElse(-1L))
                    .tags(tags).tag("network", this.network)
                    .description("Current tx count")
                    .register(registry);

            Gauge.builder("bitcoin.mempool.bytes", client, client -> mempoolInfoSupplier.get()
                            .map(it -> it.get("bytes"))
                            .flatMap(tryParseLong)
                            .orElse(-1L))
                    .tags(tags).tag("network", this.network)
                    .description("Sum of all virtual transaction sizes as defined in BIP 141. Differs from actual serialized size because witness data is discounted")
                    .register(registry);

            Gauge.builder("bitcoin.mempool.usage", client, client -> mempoolInfoSupplier.get()
                            .map(it -> it.get("usage"))
                            .flatMap(tryParseLong)
                            .orElse(-1L))
                    .tags(tags).tag("network", this.network)
                    .description("Total memory usage for the mempool")
                    .register(registry);

            Gauge.builder("bitcoin.mempool.maxmempool", client, client -> mempoolInfoSupplier.get()
                            .map(it -> it.get("maxmempool"))
                            .flatMap(tryParseLong)
                            .orElse(-1L))
                    .tags(tags).tag("network", this.network)
                    .description("Maximum memory usage for the mempool")
                    .register(registry);

            Gauge.builder("bitcoin.mempool.mempoolminfee", client, client -> mempoolInfoSupplier.get()
                            .map(it -> it.get("mempoolminfee"))
                            .flatMap(tryParseDouble)
                            .orElse(-1d))
                    .tags(tags).tag("network", this.network)
                    .description("Minimum fee rate in BTC/kB for tx to be accepted. Is the maximum of minrelaytxfee and minimum mempool fee")
                    .register(registry);

            Gauge.builder("bitcoin.mempool.minrelaytxfee", client, client -> mempoolInfoSupplier.get()
                            .map(it -> it.get("minrelaytxfee"))
                            .flatMap(tryParseDouble)
                            .orElse(-1d))
                    .tags(tags).tag("network", this.network)
                    .description("Current minimum relay fee for transactions")
                    .register(registry);
        }


        private void registerMemoryInfo(MeterRegistry registry) {
            Gauge.builder("bitcoin.memory.used", client, client -> memoryInfoSupplier.get()
                            .map(it -> it.get("used"))
                            .flatMap(tryParseLong)
                            .orElse(-1L))
                    .tags(tags).tag("network", this.network)
                    .description("Number of bytes used")
                    .register(registry);

            Gauge.builder("bitcoin.memory.free", client, client -> memoryInfoSupplier.get()
                            .map(it -> it.get("free"))
                            .flatMap(tryParseLong)
                            .orElse(-1L))
                    .tags(tags).tag("free", this.network)
                    .description("Number of bytes available in current arenas")
                    .register(registry);

            Gauge.builder("bitcoin.memory.total", client, client -> memoryInfoSupplier.get()
                            .map(it -> it.get("total"))
                            .flatMap(tryParseLong)
                            .orElse(-1L))
                    .tags(tags).tag("network", this.network)
                    .description("Total number of bytes managed")
                    .register(registry);

            Gauge.builder("bitcoin.memory.locked", client, client -> memoryInfoSupplier.get()
                            .map(it -> it.get("locked"))
                            .flatMap(tryParseLong)
                            .orElse(-1L))
                    .tags(tags).tag("network", this.network)
                    .description("Amount of bytes that succeeded locking. If this number is smaller than total, locking pages failed at some point and key data could be swapped to disk.")
                    .register(registry);

            Gauge.builder("bitcoin.memory.chunks.used", client, client -> memoryInfoSupplier.get()
                            .map(it -> it.get("chunks_used"))
                            .flatMap(tryParseLong)
                            .orElse(-1L))
                    .tags(tags).tag("network", this.network)
                    .description("Number of allocated chunks")
                    .register(registry);

            Gauge.builder("bitcoin.memory.chunks.free", client, client -> memoryInfoSupplier.get()
                            .map(it -> it.get("chunks_free"))
                            .flatMap(tryParseLong)
                            .orElse(-1L))
                    .tags(tags).tag("network", this.network)
                    .description("Number of unused chunks")
                    .register(registry);
        }
    }
}
