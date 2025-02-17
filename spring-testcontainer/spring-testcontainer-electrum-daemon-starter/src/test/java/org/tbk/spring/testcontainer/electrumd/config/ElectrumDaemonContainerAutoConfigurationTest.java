package org.tbk.spring.testcontainer.electrumd.config;

import com.google.common.base.Throwables;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.validation.ObjectError;
import org.tbk.spring.testcontainer.bitcoind.config.BitcoindContainerAutoConfiguration;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumx.config.ElectrumxContainerAutoConfiguration;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ElectrumDaemonContainerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(
                BitcoindContainerAutoConfiguration.class,
                ElectrumDaemonContainerAutoConfiguration.class
        ).withPropertyValues(
                "org.tbk.spring.testcontainer.electrum-daemon.enabled=false"
        ).run(context -> {
            assertThat(context.containsBean("electrumDaemonContainer"), is(false));

            assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(ElectrumDaemonContainer.class));
        });
    }

    @Test
    void beansAreCreated() {
        this.contextRunner.withUserConfiguration(
                BitcoindContainerAutoConfiguration.class,
                ElectrumxContainerAutoConfiguration.class,
                ElectrumDaemonContainerAutoConfiguration.class
        ).withPropertyValues(
                "org.tbk.spring.testcontainer.bitcoind.enabled=true",
                "org.tbk.spring.testcontainer.bitcoind.rpcuser=myrpcuser",
                "org.tbk.spring.testcontainer.bitcoind.rpcpassword=correcthorsebatterystaple",
                // ----------------------------------------------
                "org.tbk.spring.testcontainer.electrumx.enabled=true",
                "org.tbk.spring.testcontainer.electrumx.rpcuser=myrpcuser",
                "org.tbk.spring.testcontainer.electrumx.rpcpass=correcthorsebatterystaple",
                "org.tbk.spring.testcontainer.electrumx.rpchost=localhost",
                "org.tbk.spring.testcontainer.electrumx.rpcport=18443",
                // ----------------------------------------------
                "org.tbk.spring.testcontainer.electrum-daemon.enabled=true",
                "org.tbk.spring.testcontainer.electrum-daemon.environment.ELECTRUM_RPCPASSWORD=test"
        ).run(context -> {
            assertThat(context.containsBean("electrumDaemonContainer"), is(true));
            assertThat(context.getBean(ElectrumDaemonContainer.class), is(notNullValue()));
        });
    }

    @Test
    void throwOnInvalidPropertyConfiguration() {
        this.contextRunner.withUserConfiguration(
                BitcoindContainerAutoConfiguration.class,
                ElectrumxContainerAutoConfiguration.class,
                ElectrumDaemonContainerAutoConfiguration.class
        ).withPropertyValues(
                "org.tbk.spring.testcontainer.bitcoind.enabled=true",
                "org.tbk.spring.testcontainer.bitcoind.rpcuser=myrpcuser",
                "org.tbk.spring.testcontainer.bitcoind.rpcpassword=correcthorsebatterystaple",
                // ----------------------------------------------
                "org.tbk.spring.testcontainer.electrumx.enabled=true",
                "org.tbk.spring.testcontainer.electrumx.rpcuser=myrpcuser",
                "org.tbk.spring.testcontainer.electrumx.rpcpass=correcthorsebatterystaple",
                "org.tbk.spring.testcontainer.electrumx.rpchost=localhost",
                "org.tbk.spring.testcontainer.electrumx.rpcport=18443",
                // ----------------------------------------------
                "org.tbk.spring.testcontainer.electrum-daemon.enabled=true"
        ).run(c -> {
            Throwable startupFailure = c.getStartupFailure();
            assertThat(startupFailure, is(notNullValue()));

            Throwable cause = Throwables.getRootCause(startupFailure);
            assertThat(cause, is(instanceOf(BindValidationException.class)));

            BindValidationException e = (BindValidationException) cause;
            List<ObjectError> errors = e.getValidationErrors().getAllErrors();
            assertThat(errors, hasSize(1));

            ObjectError error = errors.get(0);
            assertThat(error.getObjectName(), is("org.tbk.spring.testcontainer.electrum-daemon"));
            assertThat(error.getCode(), is("ELECTRUM_PASSWORD.invalid"));
        });
    }
}
