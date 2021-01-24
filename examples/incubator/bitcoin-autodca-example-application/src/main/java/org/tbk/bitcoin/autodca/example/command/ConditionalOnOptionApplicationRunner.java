package org.tbk.bitcoin.autodca.example.command;

import org.springframework.boot.ApplicationArguments;

import static java.util.Objects.requireNonNull;

public abstract class ConditionalOnOptionApplicationRunner extends ConditionalOnArgsApplicationRunner {
    private final String optionName;

    public ConditionalOnOptionApplicationRunner(String optionName) {
        this.optionName = requireNonNull(optionName);
    }

    @Override
    protected boolean isEnabled(ApplicationArguments args) {
        return args.containsOption(optionName);
    }
}
