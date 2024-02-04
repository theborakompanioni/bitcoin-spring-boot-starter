package org.tbk.lnurl.auth;

import org.junit.jupiter.api.RepeatedTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

class SimpleK1FactoryTest {

    private final SimpleK1Factory sut = new SimpleK1Factory();

    @RepeatedTest(21)
    void itShouldCreateRandomK1() {
        K1 firstK1 = this.sut.create();
        assertThat(firstK1.toArray().length, is(32));

        K1 secondK1 = this.sut.create();
        assertThat(secondK1.toArray().length, is(32));

        assertThat(firstK1, not(is(secondK1)));
    }
}