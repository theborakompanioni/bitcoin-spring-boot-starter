package org.tbk.bitcoin.autodca.example.command;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.info.BuildProperties;

import static java.util.Objects.requireNonNull;

public class VersionCommandRunner extends ConditionalOnOptionApplicationRunner {
    private final BuildProperties buildProperties;

    public VersionCommandRunner(BuildProperties buildProperties) {
        super("version");
        this.buildProperties = requireNonNull(buildProperties);
    }

    @Override
    protected void doRun(ApplicationArguments args) {
        System.out.println(buildProperties.getVersion());
    }
}
