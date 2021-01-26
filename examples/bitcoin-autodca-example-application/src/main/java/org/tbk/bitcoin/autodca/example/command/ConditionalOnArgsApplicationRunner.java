package org.tbk.bitcoin.autodca.example.command;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

public abstract class ConditionalOnArgsApplicationRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        if (isEnabled(args)) {
            doRun(args);
        }
    }

    protected abstract void doRun(ApplicationArguments args);

    protected abstract boolean isEnabled(ApplicationArguments args);
}
