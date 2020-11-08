package org.tbk.bitcoin.txstats.example.score.label.impl.miner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

@Slf4j
public final class KnownPools {

    private KnownPools() {
        throw new UnsupportedOperationException();
    }

    public static List<String> knownMinorPayoutAddresses() {
        String fileName = "pools.json";
        ClassRelativeResourceLoader classRelativeResourceLoader = new ClassRelativeResourceLoader(KnownPools.class);
        Resource resource = classRelativeResourceLoader.getResource("pools.json");
        List<String> knownAddress = Lists.newArrayList();

        try (InputStream resourceInputStream = resource.getURL().openStream()) {
            JsonNode jsonNode = new ObjectMapper().reader()
                    .readTree(resourceInputStream);

            Iterator<String> payoutAddresses = jsonNode.get("payout_addresses").fieldNames();
            knownAddress.addAll(Lists.newArrayList(payoutAddresses));
        } catch (Exception e) {
            log.error("Error while loading file " + fileName, e);
        }

        return ImmutableList.copyOf(knownAddress);
    }

}
