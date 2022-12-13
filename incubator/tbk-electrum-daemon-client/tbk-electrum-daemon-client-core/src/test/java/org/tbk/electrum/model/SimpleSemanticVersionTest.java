package org.tbk.electrum.model;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.tbk.electrum.model.SimpleSemanticVersion.tryParse;

class SimpleSemanticVersionTest {

    @Test
    void itShouldNotCreateObjectOnEmptyString() {
        Optional<SemanticVersion> semanticVersion = tryParse("");
        assertThat(semanticVersion.isPresent(), is(false));
    }

    @Test
    void itShouldNotCreateObjectOnInvalidString() {
        Optional<SemanticVersion> semanticVersion = tryParse("not_a_version");
        assertThat(semanticVersion.isPresent(), is(false));
    }

    @Test
    void itShouldParseFromSimpleString() {
        SemanticVersion semanticVersion = tryParseOrThrow("1.2.3");

        assertThat(semanticVersion.getMajor(), is(1));
        assertThat(semanticVersion.getMinor(), is(2));
        assertThat(semanticVersion.getPatch(), is(3));
    }

    @Test
    void itShouldParseFromStringAllZeros() {
        SemanticVersion semanticVersion = tryParseOrThrow("0.0.0");

        assertThat(semanticVersion.getMajor(), is(0));
        assertThat(semanticVersion.getMinor(), is(0));
        assertThat(semanticVersion.getPatch(), is(0));
    }

    @Test
    void itShouldParseFromStringWithSingleNumber() {
        SemanticVersion semanticVersion = tryParseOrThrow("42");

        assertThat(semanticVersion.getMajor(), is(42));
        assertThat(semanticVersion.getMinor(), is(0));
        assertThat(semanticVersion.getPatch(), is(0));
    }

    @Test
    void itShouldThrowWhenParsingFromStringWithNegativeNumber() {
        assertThrows(IllegalStateException.class, () -> tryParseOrThrow("-42"));
    }

    @Test
    void itShouldParseFromStringWithTwoNumbers() {
        SemanticVersion semanticVersion = tryParseOrThrow("13.1337");

        assertThat(semanticVersion.getMajor(), is(13));
        assertThat(semanticVersion.getMinor(), is(1337));
        assertThat(semanticVersion.getPatch(), is(0));
    }

    @Test
    void itShouldParseFromStringWithPostfix() {
        SemanticVersion semanticVersion = tryParseOrThrow("3.3.8-snapshot.123+devel.uncommited.#c0ffee42");

        assertThat(semanticVersion.getMajor(), is(3));
        assertThat(semanticVersion.getMinor(), is(3));
        assertThat(semanticVersion.getPatch(), is(8));
    }

    @Test
    void itShouldParseFromRandomVersionString() {
        SecureRandom secureRandom = new SecureRandom();
        int randomMajor = secureRandom.nextInt(Integer.MAX_VALUE);
        int randomMinor = secureRandom.nextInt(Integer.MAX_VALUE);
        int randomPatch = secureRandom.nextInt(Integer.MAX_VALUE);

        String version = String.format("%d.%d.%d", randomMajor, randomMinor, randomPatch);
        SemanticVersion semanticVersion = tryParseOrThrow(version);

        assertThat(semanticVersion.getMajor(), is(randomMajor));
        assertThat(semanticVersion.getMinor(), is(randomMinor));
        assertThat(semanticVersion.getPatch(), is(randomPatch));
    }

    private static SemanticVersion tryParseOrThrow(String version) {
        return tryParse(version).orElseThrow(IllegalStateException::new);
    }
}
