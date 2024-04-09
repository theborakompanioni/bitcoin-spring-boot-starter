package org.tbk.spring.testcontainer.bitcoind.config;

import com.google.common.base.Throwables;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class BitcoindContainerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();
    private static final String RPC_PORT_ERROR_MSG = "'rpcport' must be in the range 0-65535 - invalid port.";

    @Test
    void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoindContainerAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.spring.testcontainer.bitcoind.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoindContainer"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(BitcoindContainer.class));
                });
    }

    @Test
    void beansAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoindContainerAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.spring.testcontainer.bitcoind.enabled=true"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoindContainer"), is(true));
                    assertThat(context.getBean(BitcoindContainer.class), is(notNullValue()));
                });
    }

    @Test
    void beansWithCustomConfigAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoindContainerAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.spring.testcontainer.bitcoind.enabled=true",
                        "org.tbk.spring.testcontainer.bitcoind.rpcuser=myrpcuser",
                        "org.tbk.spring.testcontainer.bitcoind.rpcpassword=correcthorsebatterystaple",
                        "org.tbk.spring.testcontainer.bitcoind.rpcport=18443",
                        "org.tbk.spring.testcontainer.bitcoind.commands=-printtoconsole, -debug=1, -logips=1"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoindContainer"), is(true));
                    assertThat(context.getBean(BitcoindContainer.class), is(notNullValue()));

                    BitcoindContainerProperties properties = context.getBean(BitcoindContainerProperties.class);
                    assertThat(properties, is(notNullValue()));
                    assertThat(properties.getRpcuser().orElseThrow(), is("myrpcuser"));
                    assertThat(properties.getRpcpassword().orElseThrow(), is("correcthorsebatterystaple"));

                    assertThat(properties.getCommands(), hasSize(3));
                    assertThat(properties.getCommands(), hasItem("-printtoconsole"));
                    assertThat(properties.getCommands(), hasItem("-debug=1"));
                    assertThat(properties.getCommands(), hasItem("-logips=1"));
                });
    }

    @Test
    void throwOnInvalidPropertiesValues() {
        this.contextRunner.withUserConfiguration(BitcoindContainerAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.spring.testcontainer.bitcoind.enabled=true",
                        "org.tbk.spring.testcontainer.bitcoind.rpcuser=myrpcuser",
                        "org.tbk.spring.testcontainer.bitcoind.rpcpassword=unsupported password with whitespaces",
                        "org.tbk.spring.testcontainer.bitcoind.rpcport=7777",
                        "org.tbk.spring.testcontainer.bitcoind.commands=-printtoconsole, -debug=1, -logips=1"
                )
                .run(context -> {
                    try {
                        context.start();
                        // triggers creation of container
                        BitcoindContainer<?> ignoredOnPurpose = context.getBean(BitcoindContainer.class);
                        fail("Should have failed to start application context");
                    } catch (Exception e) {
                        Throwable rootCause = Throwables.getRootCause(e);
                        assertThat(rootCause, is(instanceOf(BindValidationException.class)));

                        BindValidationException validationException = (BindValidationException) rootCause;
                        assertThat(validationException.getValidationErrors().hasErrors(), is(true));
                        assertThat(validationException.getValidationErrors().getAllErrors(), hasSize(1));
                    }
                });
    }
    
    @Test
    void throwOnLowRpcPortPropertiesValues() {
        this.contextRunner.withUserConfiguration(BitcoindContainerAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.spring.testcontainer.bitcoind.enabled=true",
                        "org.tbk.spring.testcontainer.bitcoind.rpcuser=myrpcuser",
                        "org.tbk.spring.testcontainer.bitcoind.rpcpassword=password",
                        "org.tbk.spring.testcontainer.bitcoind.rpcport=-1",
                        "org.tbk.spring.testcontainer.bitcoind.commands=-printtoconsole, -debug=1, -logips=1"
                )
                .run(context -> {
                    try {
                        context.start();
                        // triggers creation of container
                        BitcoindContainer<?> ignoredOnPurpose = context.getBean(BitcoindContainer.class);
                        fail("Should have failed to start application context");
                    } catch (Exception e) {
                        Throwable rootCause = Throwables.getRootCause(e);
                        BindValidationException validationException = (BindValidationException) rootCause;
                        
                        FieldError error = (FieldError) validationException.getValidationErrors().getAllErrors().get(0);
                        assertThat(error.getField(), is("rpcport"));
                        assertThat(error.getCode(), is("rpcport.invalid"));
                        assertThat(error.getDefaultMessage(), is(RPC_PORT_ERROR_MSG));
                    }
                });
    }
    
    @Test
    void throwOnHighRpcPortPropertiesValues() {
        this.contextRunner.withUserConfiguration(BitcoindContainerAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.spring.testcontainer.bitcoind.enabled=true",
                        "org.tbk.spring.testcontainer.bitcoind.rpcuser=myrpcuser",
                        "org.tbk.spring.testcontainer.bitcoind.rpcpassword=password",
                        "org.tbk.spring.testcontainer.bitcoind.rpcport=65536",
                        "org.tbk.spring.testcontainer.bitcoind.commands=-printtoconsole, -debug=1, -logips=1"
                )
                .run(context -> {
                    try {
                        context.start();
                        // triggers creation of container
                        BitcoindContainer<?> ignoredOnPurpose = context.getBean(BitcoindContainer.class);
                        fail("Should have failed to start application context");
                    } catch (Exception e) {
                        Throwable rootCause = Throwables.getRootCause(e);
                        BindValidationException validationException = (BindValidationException) rootCause;
                        
                        FieldError error = (FieldError) validationException.getValidationErrors().getAllErrors().get(0);
                        assertThat(error.getField(), is("rpcport"));
                        assertThat(error.getCode(), is("rpcport.invalid"));
                        assertThat(error.getDefaultMessage(), is(RPC_PORT_ERROR_MSG));
                    }
                });
    }
    
    @Test
    void throwOnWrongNetworkPropertiesValues() {
        this.contextRunner.withUserConfiguration(BitcoindContainerAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.spring.testcontainer.bitcoind.enabled=true",
                        "org.tbk.spring.testcontainer.bitcoind.rpcuser=myrpcuser",
                        "org.tbk.spring.testcontainer.bitcoind.rpcpassword=password",
                        "org.tbk.spring.testcontainer.bitcoind.rpcport=7777",
                        "org.tbk.spring.testcontainer.bitcoind.network=verizon",
                        "org.tbk.spring.testcontainer.bitcoind.commands=-printtoconsole, -debug=1, -logips=1"
                )
                .run(context -> {
                    try {
                        context.start();
                        // triggers creation of container
                        BitcoindContainer<?> ignoredOnPurpose = context.getBean(BitcoindContainer.class);
                        fail("Should have failed to start application context");
                    } catch (Exception e) {
                        Throwable rootCause = Throwables.getRootCause(e);
                        BindValidationException validationException = (BindValidationException) rootCause;
                        
                        FieldError error = (FieldError) validationException.getValidationErrors().getAllErrors().get(0);
                        assertThat(error.getField(), is("network"));
                        assertThat(error.getCode(), is("network.invalid"));
                        assertThat(error.getDefaultMessage(), is("'network' must be mainnet, regtest, or testnet - invalid network"));
                    }
                });
    }
}
