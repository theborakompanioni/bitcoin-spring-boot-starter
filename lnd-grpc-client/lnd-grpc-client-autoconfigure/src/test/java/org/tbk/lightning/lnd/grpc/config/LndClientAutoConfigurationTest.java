package org.tbk.lightning.lnd.grpc.config;

import com.google.common.base.Throwables;
import org.junit.jupiter.api.Test;
import org.lightningj.lnd.wrapper.AsynchronousLndAPI;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LndClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void beansAreCreated() {
        this.contextRunner.withUserConfiguration(LndClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.lightning.lnd.grpc.rpchost=localhost",
                        "org.tbk.lightning.lnd.grpc.rpcport=10001",
                        "org.tbk.lightning.lnd.grpc.macaroonFilePath=/dev/null",
                        "org.tbk.lightning.lnd.grpc.certFilePath=src/test/resources/lnd/tls-test.cert"
                )
                .run(context -> {
                    assertThat(context.containsBean("lndClient"), is(true));
                    assertThat(context.getBean(AsynchronousLndAPI.class), is(notNullValue()));
                });
    }

    @Test
    public void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(LndClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.lightning.lnd.grpc.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("lndClient"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(AsynchronousLndAPI.class));
                });
    }

    @Test
    public void errorIfCertFileIsMissing() {
        this.contextRunner.withUserConfiguration(LndClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.lightning.lnd.grpc.rpchost=localhost",
                        "org.tbk.lightning.lnd.grpc.rpcport=10001",
                        "org.tbk.lightning.lnd.grpc.macaroonFilePath=/dev/null",
                        "org.tbk.lightning.lnd.grpc.certFilePath=src/test/resources/lnd/tls-test-missing.cert"
                ).run(context -> {
            Throwable startupFailure = context.getStartupFailure();
            assertThat(startupFailure, is(notNullValue()));

            Throwable rootCause = Throwables.getRootCause(startupFailure);
            assertThat(rootCause.getMessage(), is("'certFile' must exist"));
        });
    }

}
