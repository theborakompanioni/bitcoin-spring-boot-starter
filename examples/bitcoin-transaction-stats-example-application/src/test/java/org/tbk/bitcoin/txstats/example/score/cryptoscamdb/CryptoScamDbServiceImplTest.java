package org.tbk.bitcoin.txstats.example.score.cryptoscamdb;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.properties.PropertyMapping;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.tbk.bitcoin.txstats.example.score.cryptoscamdb.client.EntryDto;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "org.tbk.spring.neo4j.testcontainer.enabled=false",
        "org.tbk.spring.bitcoin.testcontainer.enabled=false"
})
@ActiveProfiles("test")
public class CryptoScamDbServiceImplTest {

    @Autowired
    private CryptoScamDbService sut;

    @Test
    public void itShouldSuccessfullyFindMetaInfoForAddress() {
        String addressWithMeta = "16wd9B1LiXmTNf9hxQyb3Q9fbVHzP3NvSV";

        List<EntryDto> metaInfoOfAddress = sut.findMetaInfoOfAddress(addressWithMeta);

        assertThat(metaInfoOfAddress, hasSize(greaterThan(0)));

        EntryDto entryDto = metaInfoOfAddress.get(0);
        assertThat(entryDto.getAddress(), is(addressWithMeta));
        assertThat(entryDto.getType(), is("scam"));
        assertThat(entryDto.getCategory(), is("Scamming"));
        assertThat(entryDto.getSubcategory(), is("Trust-Trading"));
    }


    @Test
    public void itShouldNotFindMetaInfoForAddress() {
        String addressWithoutMeta = "1234567890abcdefgh";

        List<EntryDto> metaInfoOfAddress = sut.findMetaInfoOfAddress(addressWithoutMeta);

        assertThat(metaInfoOfAddress, hasSize(0));
    }
}
