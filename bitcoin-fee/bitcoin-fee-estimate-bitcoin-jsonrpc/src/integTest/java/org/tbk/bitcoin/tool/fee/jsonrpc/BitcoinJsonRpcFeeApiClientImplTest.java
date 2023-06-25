package org.tbk.bitcoin.tool.fee.jsonrpc;

import lombok.extern.slf4j.Slf4j;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j

@SpringBootTest
@ActiveProfiles("test")
class BitcoinJsonRpcFeeApiClientImplTest {

    @SpringBootApplication(proxyBeanMethods = false)
    public static class BitcoinJsonRpcFeeApiClientImplTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(BitcoinJsonRpcFeeApiClientImplTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }
    }

    @Autowired
    private BitcoinClient bitcoinJsonRpcClient;

    private BitcoinJsonRpcFeeApiClientImpl sut;

    @BeforeEach
    void setUp() {
        this.sut = new BitcoinJsonRpcFeeApiClientImpl(bitcoinJsonRpcClient);
    }

    /**
     * If no blocks are mined yet, or if there are no transactions
     * bitcoin core cannot estimate fee.
     */
    @Test
    void itShouldGetSmartFeeEstimateError() {
        EstimateSmartFeeResponse response = this.sut.estimatesmartfee(EstimateSmartFeeRequest.newBuilder()
                .setConfTarget(2)
                .build());

        assertThat(response, is(notNullValue()));
        assertThat(response.getBlocks(), is(0));
        assertThat(response.getFeerate(), is(0d));
        assertThat(response.getErrorList(), hasSize(1));

        String errorMessage = response.getError(0);
        assertThat(errorMessage, is("Insufficient data or no feerate found"));
    }

}
