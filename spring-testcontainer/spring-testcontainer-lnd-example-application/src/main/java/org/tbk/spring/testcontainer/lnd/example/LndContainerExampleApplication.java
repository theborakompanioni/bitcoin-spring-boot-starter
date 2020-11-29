package org.tbk.spring.testcontainer.lnd.example;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ApplicationListener;

import static java.util.Objects.requireNonNull;

@Slf4j
@SpringBootApplication
public class LndContainerExampleApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(LndContainerExampleApplication.class)
                .listeners(applicationPidFileWriter())
                .web(WebApplicationType.NONE)
                .profiles("development", "local")
                .run(args);
    }

    public static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }


    private final BitcoinClient bitcoinJsonRpcClient;

    public LndContainerExampleApplication(BitcoinClient bitcoinJsonRpcClient) {
        this.bitcoinJsonRpcClient = requireNonNull(bitcoinJsonRpcClient);
    }
}
