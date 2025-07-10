package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SimpleFeerate implements Feerate {

    @NonNull
    String policy;

    @NonNull
    SatPerVbyte satPerVbyte;
}
