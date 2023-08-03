package org.tbk.lightning.regtest.setup;

import fr.acinq.bitcoin.Satoshi;
import fr.acinq.lightning.MilliSatoshi;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.tbk.lightning.client.common.core.LightningCommonClient;
import org.tbk.lightning.cln.grpc.client.NodeGrpc;
import org.tbk.lightning.regtest.core.MoreMilliSatoshi;

import javax.annotation.Nullable;
import java.util.Objects;

@Value
@Builder
public
class ChannelDefinition {

    @NonNull
    LightningCommonClient<NodeGrpc.NodeBlockingStub> origin;

    @NonNull
    LightningCommonClient<NodeGrpc.NodeBlockingStub> destination;

    @NonNull
    Satoshi capacity;

    @Builder.Default
    boolean announced = true;

    @Nullable
    MilliSatoshi pushAmount;

    public MilliSatoshi getPushAmount() {
        return Objects.requireNonNullElse(pushAmount, MoreMilliSatoshi.ZERO);
    }
}
