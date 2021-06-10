package org.tbk.bitcoin.example.payreq.invoice.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class LoggingApplicationListener {

	@EventListener
	void on(Object event) {
		log.info("Received application event: " + event);
	}
}