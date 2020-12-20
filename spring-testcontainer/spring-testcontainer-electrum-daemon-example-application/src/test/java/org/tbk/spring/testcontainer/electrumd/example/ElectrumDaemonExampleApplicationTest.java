package org.tbk.spring.testcontainer.electrumd.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ElectrumDaemonExampleApplicationTest {

    @Autowired(required = false)
    private ElectrumDaemonContainer<?> electrumDaemonContainer;

    @Test
    public void contextLoads() {
        assertThat(electrumDaemonContainer, is(notNullValue()));
    }

}
