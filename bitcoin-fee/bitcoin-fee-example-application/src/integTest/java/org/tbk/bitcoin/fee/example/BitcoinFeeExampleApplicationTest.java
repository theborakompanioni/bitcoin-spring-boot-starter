package org.tbk.bitcoin.fee.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.bitcoin.tool.fee.CompositeFeeProvider;
import org.tbk.bitcoin.tool.fee.FeeProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class BitcoinFeeExampleApplicationTest {

    @Autowired(required = false)
    private FeeProvider feeProvider;

    @Test
    public void contextLoads() {
        assertThat(feeProvider, is(notNullValue()));
        assertThat(feeProvider, is(instanceOf(CompositeFeeProvider.class)));
    }

}
