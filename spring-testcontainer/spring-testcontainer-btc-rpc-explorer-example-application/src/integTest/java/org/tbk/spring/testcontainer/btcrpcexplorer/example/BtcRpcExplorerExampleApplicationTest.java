package org.tbk.spring.testcontainer.btcrpcexplorer.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.tbk.spring.testcontainer.btcrpcexplorer.BtcRpcExplorerContainer;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BtcRpcExplorerExampleApplicationTest {

    @Autowired(required = false)
    private BtcRpcExplorerContainer<?> container;

    @Test
    public void contextLoads() {
        assertThat(container, is(notNullValue()));
        assertThat(container.isRunning(), is(true));

        Boolean ranForMinimumDuration = Flux.interval(Duration.ofMillis(10))
                .map(foo -> container.isRunning())
                .filter(running -> !running)
                .timeout(Duration.ofSeconds(3), Flux.just(true))
                .blockFirst();

        assertThat("container ran for the minimum amount of time to be considered healthy", ranForMinimumDuration, is(true));
    }
}
