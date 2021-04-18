package org.tbk.bitcoin.tool.mqtt;

import io.moquette.broker.security.IAuthorizatorPolicy;
import io.moquette.broker.subscriptions.Topic;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ReadOnlyAuthorizationPolicy implements IAuthorizatorPolicy {
    @Override
    public boolean canWrite(Topic topic, String user, String client) {
        if (log.isDebugEnabled()) {
            log.debug("Reject write access to topic '{}' for user '{}' with client '{}'",
                    topic, user, client);
        }

        return false;
    }

    @Override
    public boolean canRead(Topic topic, String user, String client) {
        if (log.isDebugEnabled()) {
            log.debug("Allow read access to topic '{}' for user '{}' with client '{}'",
                    topic, user, client);
        }

        return true;
    }
}
