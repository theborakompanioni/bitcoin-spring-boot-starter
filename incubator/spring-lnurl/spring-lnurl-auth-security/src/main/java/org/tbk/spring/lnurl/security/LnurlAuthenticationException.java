package org.tbk.spring.lnurl.security;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.core.AuthenticationException;

/**
 * Thrown if an authentication request is rejected because the parameters are invalid.
 * <p>
 * Throwing this exception will not emit {@link AbstractAuthenticationFailureEvent}
 * via {@link DefaultAuthenticationEventPublisher} per default!
 * <p>
 * If you want failure events to be emitted, use an {@link AuthenticationException} class that is mapped in
 * {@link DefaultAuthenticationEventPublisher#DefaultAuthenticationEventPublisher(ApplicationEventPublisher)}
 */
public class LnurlAuthenticationException extends AuthenticationException {

    /**
     * Constructs a <code>LnurlAuthenticationException</code> with the specified message.
     *
     * @param msg the detail message
     */
    public LnurlAuthenticationException(String msg) {
        super(msg);
    }

    /**
     * Constructs a <code>LnurlAuthenticationException</code> with the specified message and
     * root cause.
     *
     * @param msg   the detail message
     * @param cause root cause
     */
    public LnurlAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
