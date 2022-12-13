package org.tbk.bitcoin.jsonrpc.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "server.port=13337",
        "management.server.port=13337",
        "management.endpoint.health.show-details=always",
        "management.health.bitcoinJsonRpc.enabled=true",
        "org.tbk.bitcoin.jsonrpc.network=regtest",
        "org.tbk.bitcoin.jsonrpc.rpchost=http://localhost",
        "org.tbk.bitcoin.jsonrpc.rpcport=13337",
        "org.tbk.bitcoin.jsonrpc.rpcuser=test",
        "org.tbk.bitcoin.jsonrpc.rpcpassword=test"
})
class EnabledBitcoinJsonRpcHealthIndicatorIntegrationTest {

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
    void itShouldCheckHealthEndpoint() throws Exception {
        // here we just check if the response is well-formed
        // as no bitcoind is running, we will end up with 503 DOWN
        mockMvc.perform(get("/actuator/health/bitcoinJsonRpc"))
                .andExpect(jsonPath("status").value(Status.DOWN.getCode()))
                .andExpect(jsonPath("details.network").exists())
                .andExpect(jsonPath("details.server").exists())
                .andExpect(jsonPath("details.error")
                        // is "java.net.ConnectException: Connection refused".. but also sometimes
                        // "java.net.ConnectException: Connection refused (Connection refused)"
                        .value(startsWith("java.net.ConnectException")))
                .andExpect(status().is(WebEndpointResponse.STATUS_SERVICE_UNAVAILABLE));
    }

    @Test
    void itShouldAddInformationToHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andDo(print())
                .andExpect(jsonPath("status").value(Status.DOWN.getCode()))
                .andExpect(jsonPath("components.bitcoinJsonRpc").exists())
                .andExpect(jsonPath("components.bitcoinJsonRpc.status").value(Status.DOWN.getCode()))
                .andExpect(jsonPath("components.bitcoinJsonRpc.details").exists())
                .andExpect(jsonPath("components.bitcoinJsonRpc.details.network").exists())
                .andExpect(jsonPath("components.bitcoinJsonRpc.details.server").exists())
                .andExpect(jsonPath("components.bitcoinJsonRpc.details.error")
                        // is "java.net.ConnectException: Connection refused".. but also sometimes
                        // "java.net.ConnectException: Connection refused (Connection refused)"
                        .value(startsWith("java.net.ConnectException")))
                .andExpect(status().is(WebEndpointResponse.STATUS_SERVICE_UNAVAILABLE));
    }
}
