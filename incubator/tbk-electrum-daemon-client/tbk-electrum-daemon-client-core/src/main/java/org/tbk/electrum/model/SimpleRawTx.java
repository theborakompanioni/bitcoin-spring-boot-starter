package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SimpleRawTx implements RawTx {

    @NonNull
    String hex;

    boolean complete;

    boolean finalized;
}
