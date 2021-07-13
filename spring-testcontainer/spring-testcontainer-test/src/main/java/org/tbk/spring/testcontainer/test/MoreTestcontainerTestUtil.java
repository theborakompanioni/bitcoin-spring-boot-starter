package org.tbk.spring.testcontainer.test;

import org.testcontainers.containers.ContainerState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkArgument;

public final class MoreTestcontainerTestUtil {

    private MoreTestcontainerTestUtil() {
        throw new UnsupportedOperationException();
    }

    public static Mono<Boolean> ranForMinimumDuration(ContainerState containerState) {
        return ranForMinimumDuration(containerState, Duration.ofSeconds(3));
    }

    public static Mono<Boolean> ranForMinimumDuration(ContainerState containerState, Duration timeoutDuration) {
        return ranForMinimumDuration(containerState, timeoutDuration, Duration.ofMillis(10));
    }

    public static Mono<Boolean> ranForMinimumDuration(ContainerState containerState, Duration timeoutDuration, Duration checkDuration) {
        checkArgument(timeoutDuration.compareTo(Duration.ZERO) > 0, "'timeoutDuration' must be positive.");
        checkArgument(checkDuration.compareTo(Duration.ZERO) > 0, "'checkDuration' must be positive.");
        checkArgument(timeoutDuration.compareTo(checkDuration) >= 0, "'timeoutDuration' must not be less than 'checkDuration'");

        return Flux.interval(checkDuration)
                .map(foo -> containerState.isRunning())
                .filter(running -> !running)
                .timeout(timeoutDuration, Flux.just(true))
                .next();
    }

}
