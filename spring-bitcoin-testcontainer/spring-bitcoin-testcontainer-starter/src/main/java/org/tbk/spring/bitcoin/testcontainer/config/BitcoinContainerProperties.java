package org.tbk.spring.bitcoin.testcontainer.config;

import com.google.common.base.Strings;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.testcontainers.shaded.com.google.common.base.CharMatcher;

@Data
@ConfigurationProperties(
        prefix = "org.tbk.spring.bitcoin.testcontainer",
        ignoreUnknownFields = false
)
public class BitcoinContainerProperties implements Validator {

    /**
     * Whether the client should be enabled
     */
    private boolean enabled;

    /**
     * RPC username
     */
    private String rpcuser;

    /**
     * RPC password
     */
    private String rpcpassword;


    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == BitcoinContainerProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        BitcoinContainerProperties properties = (BitcoinContainerProperties) target;

        String rpcuserValue = properties.getRpcuser();
        if (Strings.isNullOrEmpty(rpcuserValue)) {
            String errorMessage = String.format("'rpcuser' must not be empty - invalid value: '%s'", rpcuserValue);
            errors.rejectValue("rpcuser", "rpcuser.invalid", errorMessage);
        } else if (containsWhitespaces(rpcuserValue)) {
            String errorMessage = String.format("'rpcuser' must not contain whitespaces - invalid value: '%s'", rpcuserValue);
            errors.rejectValue("rpcuser", "rpcuser.invalid", errorMessage);
        }

        String rpcpasswordValue = properties.getRpcpassword();
        if (Strings.isNullOrEmpty(rpcpasswordValue)) {
            String errorMessage = "'rpcpassword' must not be empty";
            errors.rejectValue("rpcpassword", "rpcpassword.invalid", errorMessage);
        } else if (containsWhitespaces(rpcpasswordValue)) {
            String errorMessage = "'rpcpassword' must not contain whitespaces";
            errors.rejectValue("rpcuser", "rpcuser.invalid", errorMessage);
        }
    }

    private static boolean containsWhitespaces(String value) {
        return CharMatcher.WHITESPACE.matchesAnyOf(value);
    }
}

