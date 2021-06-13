package org.tbk.bitcoin.currency;

import org.javamoney.moneta.Money;
import org.javamoney.moneta.function.MonetaryOperators;
import org.javamoney.tck.JSR354TestConfiguration;
import org.javamoney.tck.TCKRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mutabilitydetector.internal.com.google.common.collect.ImmutableList;
import org.springframework.util.FileSystemUtils;

import javax.money.Monetary;
import javax.money.MonetaryOperator;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * See https://github.com/JavaMoney/jsr354-tck/blob/master/src/main/asciidoc/userguide.adoc
 * for a complete user guide on how to use the technical compatibility kit.
 */
public class Jsr354TechnicalCompatibilityKitTest {
    private static final Path targetPath = Path.of("build/reports/tck-target");

    @BeforeEach
    void setUp() throws IOException {
        FileSystemUtils.deleteRecursively(targetPath);
        Path directory = Files.createDirectory(targetPath);

        System.setProperty("outputDir", directory.toString() + "/tck-output");
        System.setProperty("reportFile", directory.toString() + "/tck-results.txt");
    }

    @Test
    void runTCK() {
        TCKRunner.main();
    }

    public static class BitcoinJSR354TestConfiguration implements JSR354TestConfiguration {
        @Override
        public Collection<Class> getAmountClasses() {
            return ImmutableList.of(Money.class);
        }

        @Override
        public Collection<Class> getCurrencyClasses() {
            return ImmutableList.of(BitcoinCurrencyUnit.class);
        }

        @Override
        public Collection<MonetaryOperator> getMonetaryOperators4Test() {
            List<MonetaryOperator> ops = new ArrayList<>();
            ops.add(Monetary.getDefaultRounding());
            ops.add(MonetaryOperators.rounding(0));
            ops.add(MonetaryOperators.rounding(8));
            ops.add(MonetaryOperators.rounding(12));
            ops.add(MonetaryOperators.majorPart());
            ops.add(MonetaryOperators.minorPart());
            ops.add(MonetaryOperators.percent(BigDecimal.ONE));
            ops.add(MonetaryOperators.percent(3.5));
            ops.add(MonetaryOperators.permil(10.3));
            ops.add(MonetaryOperators.permil(BigDecimal.ONE));
            ops.add(MonetaryOperators.permil(10.5, MathContext.DECIMAL32));
            ops.add(MonetaryOperators.reciprocal());

            //ops.add(MonetaryConversions.getConversion("EUR"));

            return ops;
        }
    }
}

