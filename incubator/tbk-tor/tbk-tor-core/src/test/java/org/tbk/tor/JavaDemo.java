package org.tbk.tor;

import lombok.extern.slf4j.Slf4j;
import org.berndpruenster.netlayer.tor.HiddenServiceSocket;
import org.berndpruenster.netlayer.tor.NativeTor;
import org.berndpruenster.netlayer.tor.Tor;
import org.berndpruenster.netlayer.tor.TorSocket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class JavaDemo {

    private final int port = 21001;

    NativeTorFactory sut;

    NativeTor nativeTor;

    @Before
    public void setUp() {
        this.sut = new NativeTorFactory();

        this.nativeTor = sut.create();

        //set default instance, so it can be omitted whenever creating Tor (Server)Sockets
        Tor.setDefault(this.nativeTor);
    }

    @After
    public void tearDown() {
        this.nativeTor.shutdown();
    }

    @Test
    public void test() throws InterruptedException {
        //create a hidden service in directory 'test' inside the tor installation directory
        HiddenServiceSocket hiddenServiceSocket = new HiddenServiceSocket(port, "test");


        Mono<Boolean> socketReady = Mono.create(fluxSink -> {
            //it takes some time for a hidden service to be ready, so adding a listener only after creating the HS is not an issue
            hiddenServiceSocket.addReadyListener(socket -> {
                fluxSink.success(true);
                return null;
            });
        });

        socketReady.blockOptional(Duration.ofMinutes(5));

        System.out.println("Hidden Service " + hiddenServiceSocket + " is ready");

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
            log.info("we'll try and connect to the just-published hidden service");
            try (TorSocket s1 = new TorSocket(hiddenServiceSocket.getSocketAddress(), "Foo")) {
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

        countDownLatch.await(3, TimeUnit.MINUTES);
    }


    private static Collection<String> parseBridgeLines(String file) throws IOException {
        if (file == null) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            List<String> lines = new LinkedList<>();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        }
    }

}