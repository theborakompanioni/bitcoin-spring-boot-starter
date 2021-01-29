package org.tbk.bitcoin.autodca.example.command;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.info.BuildProperties;

import static java.util.Objects.requireNonNull;

public class HelpCommandRunner extends ConditionalOnOptionApplicationRunner {
    private final BuildProperties buildProperties;

    public HelpCommandRunner(BuildProperties buildProperties) {
        super("help");
        this.buildProperties = requireNonNull(buildProperties);
    }

    @Override
    protected void doRun(ApplicationArguments args) {
        System.out.println("Usage: java -jar autodca.jar [OPTION...] COMMAND...");
        System.out.println();
        System.out.println("  --dry                   emit debugging messages");
        System.out.println("  --help                  print this help text");
        System.out.println("  --version               print application version");
        System.out.println();
        System.out.println("  Version");
        System.out.println("      " + buildProperties.getVersion());
        System.out.println();
        System.out.println("  Command");
        System.out.println("      stack               stack sats - place market order with the configured amount");
        System.out.println("      withdraw            withdraw funds - sends crypto to the configured address");
        System.out.println("      balance             displays the balance on the exchange");
        System.out.println("      ticker              displays the ticker (bid, ask, high, low, open, last)");
        System.out.println("      history             displays the latest trades");
        System.out.println();
        System.out.println("  Examples");
        System.out.println("      java -jar autodca.jar --help");
        System.out.println("      java -jar autodca.jar --version");
        System.out.println("      java -jar autodca.jar --dry stack");
        System.out.println("      java -jar autodca.jar history");
        System.out.println();
    }
}
