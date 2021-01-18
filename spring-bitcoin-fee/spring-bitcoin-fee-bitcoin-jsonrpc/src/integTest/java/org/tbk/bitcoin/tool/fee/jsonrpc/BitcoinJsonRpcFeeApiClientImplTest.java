package org.tbk.bitcoin.tool.fee.jsonrpc;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class BitcoinJsonRpcFeeApiClientImplTest {

    @SpringBootApplication
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

    @Before
    public void setUp() {
        this.sut = new BitcoinJsonRpcFeeApiClientImpl(bitcoinJsonRpcClient);
    }

    /**
     * If no blocks are mined yet, or if there are no transactions
     * bitcoin core cannot estimate fee.
     */
    @Test
    public void itShouldGetSmartFeeEstimateError() {
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
