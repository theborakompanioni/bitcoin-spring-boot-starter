package org.tbk.bitcoin.fee.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.tbk.bitcoin.tool.fee.CompositeFeeProvider;
import org.tbk.bitcoin.tool.fee.FeeProvider;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
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
