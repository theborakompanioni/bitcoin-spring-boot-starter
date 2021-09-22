package org.tbk.lightning.lnd.grpc.config;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "server.port=13337",
        "management.server.port=13337",
        "management.endpoints.web.exposure.include=metrics",
        "org.tbk.lightning.lnd.grpc.rpchost=localhost",
        "org.tbk.lightning.lnd.grpc.rpcport=13337",
        "org.tbk.lightning.lnd.grpc.macaroonFilePath=/dev/null",
        "org.tbk.lightning.lnd.grpc.certFilePath=src/test/resources/lnd/tls-test.cert"
})
public class LndMetricsIntegrationTest {

    @SpringBootApplication
    public static class BitcoinJsonRpcTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(BitcoinJsonRpcTestApplication.class)
                    .web(WebApplicationType.SERVLET)
                    .run(args);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void itShouldAddMetricsEndpoints() throws Exception {
        List<String> metricNames = ImmutableList.<String>builder()
                .add("lnd.blocks.height")
                .add("lnd.channels.active")
                .add("lnd.channels.inactive")
                .add("lnd.channels.pending")
                .add("lnd.peers")
                .build();

        for (String metricName : metricNames) {
            mockMvc.perform(get("/actuator/metrics/{metricName}", metricName))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }
}