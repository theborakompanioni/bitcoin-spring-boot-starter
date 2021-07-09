package org.tbk.bitcoin.example.payreq.common.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class LoggingApplicationListener {

    @EventListener
    void on(Object event) {
        log.trace("Received application event: {}", event);
    }
}