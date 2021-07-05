package org.tbk.electrum.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Value;

@Value
public class DaemonStatusRequest {
    private static final DaemonStatusRequest INSTANCE = new DaemonStatusRequest();

    public static DaemonStatusRequest create() {
        // class is immutable - same instance can safely be returned
        return INSTANCE;
    }

    @JsonProperty("subcommand")
    @SuppressFBWarnings("SS_SHOULD_BE_STATIC")
    String subcommand = "status";
}
