package org.tbk.bitcoin.txstats.example.score.bitcoinabuse;

import org.tbk.bitcoin.tool.btcabuse.CheckResponseDto;
import org.tbk.bitcoin.tool.btcabuse.client.BtcAbuseApiClient;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class BtcAbuseServiceImpl implements BtcAbuseService {

    private final BtcAbuseApiClient client;

    public BtcAbuseServiceImpl(BtcAbuseApiClient client) {
        this.client = requireNonNull(client);
    }

    @Override
    public List<CheckResponseDto> findMetaInfoOfAddress(String address) {
        CheckResponseDto check = this.client.check(address);
        if (check.getCount() <= 0) {
            return Collections.emptyList();
        }

        return Collections.singletonList(check);
    }
}
