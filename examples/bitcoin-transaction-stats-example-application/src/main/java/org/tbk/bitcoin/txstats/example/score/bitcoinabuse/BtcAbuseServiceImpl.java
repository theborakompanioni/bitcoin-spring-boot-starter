package org.tbk.bitcoin.txstats.example.score.bitcoinabuse;

import com.google.common.collect.ImmutableMap;
import org.tbk.bitcoin.txstats.example.score.bitcoinabuse.client.BtcAbuseApiClient;
import org.tbk.bitcoin.txstats.example.score.bitcoinabuse.client.CheckResponseDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class BtcAbuseServiceImpl implements BtcAbuseService {

    private final BtcAbuseApiClient client;

    public BtcAbuseServiceImpl(BtcAbuseApiClient client) {
        this.client = requireNonNull(client);
    }

    @Override
    public List<Map<String, Object>> findMetaInfoOfAddress(String address) {
        CheckResponseDto check = this.client.check(address);
        if (check.getCount() <= 0) {
            return Collections.emptyList();
        }

        return Collections.singletonList(ImmutableMap.<String, Object>builder()
                .put("address", check.getAddress())
                .put("count", check.getCount())
                .build());
    }
}
