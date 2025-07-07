package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.tbk.electrum.command.OnchainCapitalGainsResponse;

import static org.tbk.electrum.model.BtcTxoValues.fromBtcStringOrZero;

@Value
@Builder
public class SimpleOnchainSummary implements OnchainSummary {
    public static SimpleOnchainSummary from(OnchainCapitalGainsResponse val) {
        return SimpleOnchainSummary.builder()
                .startBalance(fromBtcStringOrZero(val.getBegin().map(OnchainCapitalGainsResponse.PointInTimeStats::getBalance).orElse(null)))
                .endBalance(fromBtcStringOrZero(val.getEnd().map(OnchainCapitalGainsResponse.PointInTimeStats::getBalance).orElse(null)))
                .incoming(fromBtcStringOrZero(val.getFlow().map(OnchainCapitalGainsResponse.FlowStats::getIncoming).orElse(null)))
                .outgoing(fromBtcStringOrZero(val.getFlow().map(OnchainCapitalGainsResponse.FlowStats::getOutgoing).orElse(null)))
                .build();
    }

    @NonNull
    TxoValue startBalance;

    @NonNull
    TxoValue endBalance;

    @NonNull
    TxoValue incoming;

    @NonNull
    TxoValue outgoing;
}
