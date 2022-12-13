package org.tbk.bitcoin.jsonrpc.config;

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
        "org.tbk.bitcoin.jsonrpc.network=regtest",
        "org.tbk.bitcoin.jsonrpc.rpchost=http://localhost",
        "org.tbk.bitcoin.jsonrpc.rpcport=13337",
        "org.tbk.bitcoin.jsonrpc.rpcuser=test",
        "org.tbk.bitcoin.jsonrpc.rpcpassword=test"
})
class BitcoinJsonRpcClientMetricsIntegrationTest {

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
    void itShouldAddMetricsEndpoints() throws Exception {
        List<String> metricNames = ImmutableList.<String>builder()
                .add("bitcoin.blockchain.blocks")
                .add("bitcoin.blockchain.difficulty")
                .add("bitcoin.blockchain.headers")
                .add("bitcoin.blockchain.verification.progress")
                .add("bitcoin.memory.chunks.free")
                .add("bitcoin.memory.chunks.used")
                .add("bitcoin.memory.free")
                .add("bitcoin.memory.locked")
                .add("bitcoin.memory.total")
                .add("bitcoin.memory.used")
                .add("bitcoin.mempool.bytes")
                .add("bitcoin.mempool.maxmempool")
                .add("bitcoin.mempool.mempoolminfee")
                .add("bitcoin.mempool.minrelaytxfee")
                .add("bitcoin.mempool.size")
                .add("bitcoin.mempool.usage")
                .add("bitcoin.network.connections")
                .add("bitcoin.network.timeoffset")
                .add("bitcoin.network.version")
                .build();

        for (String metricName : metricNames) {
            mockMvc.perform(get("/actuator/metrics/{metricName}", metricName))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }
}
