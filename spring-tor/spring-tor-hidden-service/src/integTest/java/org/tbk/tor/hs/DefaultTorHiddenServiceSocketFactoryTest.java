package org.tbk.tor.hs;

import lombok.extern.slf4j.Slf4j;
import org.berndpruenster.netlayer.tor.HiddenServiceSocket;
import org.berndpruenster.netlayer.tor.NativeTor;
import org.berndpruenster.netlayer.tor.Tor;
import org.berndpruenster.netlayer.tor.TorSocket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tbk.tor.NativeTorFactory;
import org.tbk.tor.hs.TorHiddenServiceSocketFactory.HiddenServiceSocketCreateContext;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Slf4j
public class DefaultTorHiddenServiceSocketFactoryTest {

    private final int port = 21011;

    private DefaultTorHiddenServiceSocketFactory sut;

    private NativeTor nativeTor;

    @BeforeEach
    public void setUp() {
        File workingDirectory = new File("build/tmp/tor-working-dir");
        NativeTorFactory torFactory = new NativeTorFactory(workingDirectory);

        this.nativeTor = torFactory.create().blockOptional(Duration.ofSeconds(30))
                .orElseThrow(() -> new IllegalStateException("Could not start tor"));

        // set default instance, so it can be omitted whenever creating Tor (Server)Sockets
        Tor.setDefault(this.nativeTor);

        this.sut = new DefaultTorHiddenServiceSocketFactory(this.nativeTor);
    }

    @AfterEach
    public void tearDown() {
        this.nativeTor.shutdown();
    }

    @Test
    public void itShouldCreateHiddenService() throws InterruptedException {
        // create a hidden service in directory 'test' inside the tor installation directory
        HiddenServiceSocketCreateContext context = HiddenServiceSocketCreateContext.builder()
                .hostPort(port)
                .hiddenServiceDir("test")
                .build();

        HiddenServiceSocket hiddenServiceSocket = this.sut.createReady(context)
                .blockOptional(Duration.ofMinutes(3))
                .orElseThrow(() -> new IllegalStateException("Could not create hidden service on port " + port));

        log.info("Hidden Service {} is ready", hiddenServiceSocket);

        Flux<Socket> socketFlux = Flux.<Socket>create(fluxSink -> {
            try {
                fluxSink.next(hiddenServiceSocket.accept());
            } catch (IOException e) {
                fluxSink.error(e);
            }
        }).onErrorContinue((e, target) -> {
            log.warn("Error while accepting socket connections: {}", e.getMessage());
        });

        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            log.info("trying to connect to the hidden service {}", hiddenServiceSocket);
            try (TorSocket s1 = new TorSocket(hiddenServiceSocket.getSocketAddress(), "foo")) {
                log.info("Successfully connected to {}", hiddenServiceSocket);

                log.info("Closing socket now...");
                hiddenServiceSocket.close();
            } catch (Exception e) {
                log.error("Exception while handling socket connection");
            }

            // retry connecting
            try (TorSocket s2 = new TorSocket(hiddenServiceSocket.getServiceName(), hiddenServiceSocket.getHiddenServicePort(), "Foo")) {
                // do nothing on purpose
            } catch (Exception e) {
                log.debug("As expected, connection to {} failed with: {}", hiddenServiceSocket, e.getMessage());
            }

            countDownLatch.countDown();
        }).start();

        Socket incomingSocket = socketFlux.blockFirst(Duration.ofSeconds(10));
        log.info("Got an incoming connection to socket {}: {}", hiddenServiceSocket, incomingSocket);

        boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
        assertThat("socket has been accepted", await, is(true));

        try {
            incomingSocket.close();
        } catch (IOException e) {
            log.warn("Error while closing hidden service socket", e);
        }
    }

}