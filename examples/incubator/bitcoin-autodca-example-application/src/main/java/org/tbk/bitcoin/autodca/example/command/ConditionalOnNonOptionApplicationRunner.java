package org.tbk.bitcoin.autodca.example.command;

import org.springframework.boot.ApplicationArguments;

import static java.util.Objects.requireNonNull;

public abstract class ConditionalOnNonOptionApplicationRunner extends ConditionalOnArgsApplicationRunner {
    private final String optionName;

    public ConditionalOnNonOptionApplicationRunner(String optionName) {
        this.optionName = requireNonNull(optionName);
    }

    @Override
    protected boolean isEnabled(ApplicationArguments args) {
        return args.getNonOptionArgs().contains(optionName);
    }
}
