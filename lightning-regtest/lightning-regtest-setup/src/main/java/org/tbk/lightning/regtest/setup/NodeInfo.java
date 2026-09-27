package org.tbk.lightning.regtest.setup;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.tbk.lightning.client.common.core.LightningCommonClient;

@Value
@Builder
public class NodeInfo {

    @NonNull
    String hostname;

    @NonNull
    Integer p2pPort;

    @NonNull
    LightningCommonClient client;
}
