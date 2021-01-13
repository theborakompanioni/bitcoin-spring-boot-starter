package org.tbk.xchange.spring.config;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import lombok.Data;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeClassUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Properties class for XChange Exchange Specifications.
 * See {@link org.knowm.xchange.ExchangeSpecification}.
 */
@Data
public class ExchangeSpecificationProperties implements Validator {

    /**
     * The the fully-qualified class name of the exchange.
     * e.g. org.knowm.xchange.kraken.KrakenExchange
     */
    private String exchangeClass;

    /**
     * Set the exchange name (e.g. "Mt Gox").
     */
    private String exchangeName;
    /**
     * Set the exchange description (e.g. "Major exchange specialising in USD, EUR, GBP").
     */
    private String exchangeDescription;

    /**
     * Set the username for authentication.
     */
    private String userName;

    /**
     * Set the password for authentication.
     */
    private String password;

    /**
     * Set the API secret key typically used in HMAC signing of requests. For MtGox this would be the
     * "Rest-Sign" field.
     */
    private String secretKey;

    /**
     * Set the API key. For MtGox this would be the "Rest-Key" field.
     */
    private String apiKey;

    /**
     * Set the URI to reach the <b>root</b> of the exchange API for SSL queries (e.g. use
     * "https://example.com:8443/exchange", not "https://example.com:8443/exchange/api/v3/trades").
     */
    private String sslUri;

    /**
     * Set the URI to reach the <b>root</b> of the exchange API for plaintext (non-SSL) queries (e.g.
     * use "http://example.com:8080/exchange", not "http://example.com:8080/exchange/api/v3/trades")
     */
    private String plainTextUri;

    /**
     * Set the host name of the server providing data (e.g. "mtgox.com").
     */
    private String host;

    /**
     * Set the port number of the server providing direct socket data (e.g. "1337").
     */
    private Integer port;

    /**
     * Set the host name of the http proxy server.
     */
    private String proxyHost;

    /**
     * Set the port of the http proxy server.
     */
    private Integer proxyPort;

    /**
     * Set the http connection timeout for the connection.
     */
    private Integer httpConnTimeout;

    /**
     * Set the http read timeout for the connection.
     */
    private Integer httpReadTimeout = 0;

    /**
     * Set retry and rate limit values.
     */
    private ResilienceSpecificationProperties resilience;

    /**
     * Set the override file for generating the {@link org.knowm.xchange.dto.meta.ExchangeMetaData} object.
     */
    private String metaDataJsonFileOverride;

    /**
     * By default, some meta data from the exchange is remotely loaded (if implemented). Here you can set this default behavior.
     */
    private Boolean shouldLoadRemoteMetaData;

    /**
     * Set the arbitrary exchange-specific parameters to be passed to the exchange implementation.
     */
    private Map<String, Object> exchangeSpecificParameters;

    public Class<? extends Exchange> getExchangeClassOrThrow() {
        return ExchangeClassUtils.exchangeClassForName(this.getExchangeClass());
    }

    public Map<String, Object> getExchangeSpecificParameters() {
        return this.exchangeSpecificParameters != null ? ImmutableMap.copyOf(exchangeSpecificParameters) : Collections.emptyMap();
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == ExchangeSpecificationProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        ExchangeSpecificationProperties properties = (ExchangeSpecificationProperties) target;

        if (Strings.isNullOrEmpty(properties.getExchangeClass())) {
            String errorMessage = String.format("'exchangeClass' must not be empty - invalid value: %s", properties.getExchangeClass());
            errors.rejectValue("exchangeClass", "exchangeClass.invalid", errorMessage);
        } else {
            try {
                Class<?> aClass = Class.forName(properties.getExchangeClass());

                boolean isXChangeClass = Exchange.class.isAssignableFrom(aClass);
                if (!isXChangeClass) {
                    String errorMessage = String.format("'exchangeClass' is not a subclass of %s - value: %s",
                            Exchange.class.getName(), properties.getExchangeClass());
                    errors.rejectValue("exchangeClass", "exchangeClass.invalid", errorMessage);
                }
            } catch (ClassNotFoundException e) {
                String errorMessage = String.format("'exchangeClass' could not be loaded - value: %s", properties.getExchangeClass());
                errors.rejectValue("exchangeClass", "exchangeClass.invalid", errorMessage);
            }
        }
    }

    @Data
    public static class ResilienceSpecificationProperties {
        /**
         * Flag that lets you enable retry functionality if it was implemented for the given exchange.
         */
        private Boolean retryEnabled;

        /**
         * Flag that lets you enable call rate limiting functionality if it was implemented for the given exchange.
         */
        private Boolean rateLimiterEnabled;
    }
}