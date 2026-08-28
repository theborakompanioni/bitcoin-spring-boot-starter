package org.tbk.bitcoin.example.payreq.lnd.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
class LndApiTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LndApi sut;

    @Test
    void addInvoiceSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/lnd/invoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "msats": 2100,
                                    "memo": "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks"
                                }
                                """))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("bolt11").value(startsWith("lnbcrt")))
                .andExpect(jsonPath("raw").exists())
                .andExpect(status().isOk());
    }

    @Test
    void infoSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/lnd/info")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version", is(notNullValue())))
                .andExpect(jsonPath("$.commitHash", is(notNullValue())))
                .andExpect(jsonPath("$.identityPubkey", is(notNullValue())))
                .andExpect(jsonPath("$.numPendingChannels", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.numActiveChannels", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.numInactiveChannels", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.numPeers", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.blockHeight", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.chains[0].chain", is("bitcoin")))
                .andExpect(jsonPath("$.chains[0].network", is("regtest")));
    }

    @Test
    void feeReportSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/lnd/fee/report")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayFeeSum", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.weekFeeSum", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.monthFeeSum", is(greaterThanOrEqualTo(0))));
    }

    @Test
    void channelBalanceSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/lnd/channel/balance")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.pendingOpenBalance", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.localBalance.msat", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.remoteBalance.msat", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.unsettledLocalBalance.msat", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.unsettledRemoteBalance.msat", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.pendingOpenLocalBalance.msat", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.pendingOpenRemoteBalance.msat", is(greaterThanOrEqualTo(0))));
    }

    @Test
    void walletBalanceSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/lnd/wallet/balance")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBalance", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.confirmedBalance", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.unconfirmedBalance", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.lockedBalance", is(greaterThanOrEqualTo(0))));
    }
}