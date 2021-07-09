package org.tbk.bitcoin.example.payreq.api;

import org.bitcoinj.core.Address;
import org.bitcoinj.params.RegTestParams;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.tbk.bitcoin.example.payreq.payment.api.PaymentRequestApi;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
class PaymentRequestApiTest {
    private static final Address address = Address.fromString(RegTestParams.get(), "bcrt1q4m4fds2rdtgde67ws5aema2a2wqvv7uzyxqc4j");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRequestApi sut;

    /**
     * Just test for error status here.
     * Custom error handling is not picked up by MockMvc
     * {@see https://github.com/alimate/errors-spring-boot-starter/issues/79#issuecomment-496019863}
     * Quote:
     * > [...] you shouldn't be testing Spring Boot's error handling. If you're customizing it in any way you can write
     * > Spring Boot integration tests (with an actual container) to verify error responses. And then for MockMvc tests
     * > focus on fully testing the web layer while expecting exceptions to bubble up.
     */
    @Test
    void paymentRequestJsonMissingAddressParamError() throws Exception {
        mockMvc.perform(get("/api/v1/payment/request")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void paymentRequestJsonSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/payment/request")
                .queryParam("network", "regtest")
                .queryParam("address", address.toString())
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentUrl", is("bitcoin:" + address)));
    }

    /**
     * Test for response of a html message.
     */
    @Test
    void paymentRequestHtmlSuccess() throws Exception {
        String expectedImgElement = String.format("<img src=\"/api/v1/payment/request/qrcode?network=regtest&address=%s\" alt=\"bitcoin:%s\" />", address, address);

        mockMvc.perform(get("/api/v1/payment/request")
                .queryParam("network", "regtest")
                .queryParam("address", address.toString())
                .accept(MediaType.TEXT_HTML))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(expectedImgElement)));
    }

    @Test
    void paymentRequestQrCodeSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/payment/request/qrcode")
                .queryParam("network", "regtest")
                .queryParam("address", address.toString())
                .accept(MediaType.IMAGE_PNG))
                .andExpect(status().isOk());
    }
}