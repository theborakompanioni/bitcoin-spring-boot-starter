package org.tbk.electrum.command;

import lombok.Value;

@Value
public class DaemonStatusRequest {
    private static final DaemonStatusRequest INSTANCE = new DaemonStatusRequest();

    public static DaemonStatusRequest create() {
        // class is immutable - same instance can safely be returned
        return INSTANCE;
    }
}
