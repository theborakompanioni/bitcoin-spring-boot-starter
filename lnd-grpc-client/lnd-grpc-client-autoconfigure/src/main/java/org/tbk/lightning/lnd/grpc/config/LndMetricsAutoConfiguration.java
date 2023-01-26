package org.tbk.lightning.lnd.grpc.config;

import com.google.common.base.Suppliers;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.common.lang.NonNullApi;
import io.micrometer.common.lang.NonNullFields;
import lombok.extern.slf4j.Slf4j;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.GetInfoResponse;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.lightning.lnd.grpc.LndRpcConfig;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "org.tbk.lightning.lnd.grpc.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({
        MeterBinder.class,
        LndRpcConfig.class
})
@AutoConfigureAfter(LndClientAutoConfiguration.class)
public class LndMetricsAutoConfiguration {

    @Bean
    @ConditionalOnBean(SynchronousLndAPI.class)
    public LndClientMetrics lndClientMetrics(SynchronousLndAPI client) {
        return new LndClientMetrics(client);
    }

    @Slf4j
    @NonNullApi
    @NonNullFields
    public static class LndClientMetrics implements MeterBinder {
        private final SynchronousLndAPI client;
        private final Iterable<Tag> tags;

        private final Supplier<Optional<GetInfoResponse>> infoSupplier = Suppliers
                .memoizeWithExpiration(this::fetchInfo, 1, TimeUnit.SECONDS);

        public LndClientMetrics(SynchronousLndAPI client) {
            this(client, Tags.empty());
        }

        public LndClientMetrics(SynchronousLndAPI client, Iterable<Tag> tags) {
            this.client = requireNonNull(client);
            this.tags = requireNonNull(tags);
        }

        @Override
        public void bindTo(MeterRegistry registry) {
            Gauge.builder("lnd.blocks.height", client, client -> infoSupplier.get()
                    .map(GetInfoResponse::getBlockHeight)
                    .orElse(-1))
                    .tags(tags)
                    .description("Number of all blocks in the chain")
                    .register(registry);

            Gauge.builder("lnd.channels.active", client, client -> infoSupplier.get()
                    .map(GetInfoResponse::getNumActiveChannels)
                    .orElse(-1))
                    .tags(tags)
                    .description("Number of active channels")
                    .register(registry);

            Gauge.builder("lnd.channels.inactive", client, client -> infoSupplier.get()
                    .map(GetInfoResponse::getNumInactiveChannels)
                    .orElse(-1))
                    .tags(tags)
                    .description("Number of inactive channels")
                    .register(registry);

            Gauge.builder("lnd.channels.pending", client, client -> infoSupplier.get()
                    .map(GetInfoResponse::getNumPendingChannels)
                    .orElse(-1))
                    .tags(tags)
                    .description("Number of pending channels")
                    .register(registry);

            Gauge.builder("lnd.peers", client, client -> infoSupplier.get()
                    .map(GetInfoResponse::getNumPeers)
                    .orElse(-1))
                    .tags(tags)
                    .description("Number of peers")
                    .register(registry);
        }

        private Optional<GetInfoResponse> fetchInfo() {
            try {
                return Optional.of(client.getInfo());
            } catch (StatusException | ValidationException e) {
                log.warn("Error while fetching 'info' from lnd: {}", e.getMessage());
                return Optional.empty();
            }
        }
    }
}