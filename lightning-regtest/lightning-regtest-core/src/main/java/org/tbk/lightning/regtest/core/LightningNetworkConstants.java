package org.tbk.lightning.regtest.core;

import fr.acinq.bitcoin.Satoshi;
import fr.acinq.lightning.MilliSatoshi;

import java.time.Duration;

public final class LightningNetworkConstants {
    // 16_777_215 sats is the largest size (unless large channels were negotiated with the peer)
    // See: https://lightning.readthedocs.io/lightning-fundchannel.7.html
    public static final Satoshi LARGEST_CHANNEL_SIZE = new Satoshi(16_777_215L);
    public static final MilliSatoshi LARGEST_CHANNEL_SIZE_MSAT = new MilliSatoshi(LARGEST_CHANNEL_SIZE);

    public static final Duration CLN_DEFAULT_INVOICE_EXPIRY = Duration.ofSeconds(604800);

    public static final int CLN_DEFAULT_REGTEST_P2P_PORT = 19_846;
    public static final int LND_DEFAULT_REGTEST_P2P_PORT = 9_735;

    public static final int CLN_DEFAULT_CHANNEL_FUNDING_TX_MIN_CONFIRMATIONS = 6;

    private LightningNetworkConstants() {
        throw new UnsupportedOperationException();
    }
}
