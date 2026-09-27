package org.tbk.lightning.regtest.core;

import fr.acinq.lightning.MilliSatoshi;

public final class MoreMilliSatoshi {
    public static final MilliSatoshi ZERO = new MilliSatoshi(0L);

    private MoreMilliSatoshi() {
        throw new UnsupportedOperationException();
    }
}
