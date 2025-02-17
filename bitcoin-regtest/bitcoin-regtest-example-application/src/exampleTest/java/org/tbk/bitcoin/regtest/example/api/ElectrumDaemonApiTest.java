package org.tbk.bitcoin.regtest.example.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
class ElectrumDaemonApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ElectrumDaemonApi electrumDaemonApi;

    @Test
    void itShouldFetchStatusSuccessfully() throws Exception {
        mockMvc.perform(get("/api/v1/electrum/status")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path", is("/home/electrum/.electrum/regtest")))
                .andExpect(jsonPath("$.server", is("electrumx_regtest")))
                .andExpect(jsonPath("$.blockchain_height", is(greaterThanOrEqualTo(-1))))
                .andExpect(jsonPath("$.server_height", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.spv_nodes", is(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.connected", is(true)))
                .andExpect(jsonPath("$.auto_connect", is(true)))
                .andExpect(jsonPath("$.version", is(notNullValue())))
                //.andExpect(jsonPath("$.wallets", is(notNullValue())))
                .andExpect(jsonPath("$.fee_per_kb", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.default_wallet", is(anyOf(nullValue(), notNullValue()))))
                .andExpect(jsonPath("$.current_wallet", is(anyOf(nullValue(), notNullValue()))));
    }
}