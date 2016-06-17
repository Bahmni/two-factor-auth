package org.bahmni.auth.twofactor;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class HelloTest {

    @Test
    public void shouldGreetMeWithHello() {
        Hello hello = new Hello();

        String greetings = hello.greet();

        assertThat(greetings, is("Hello World!"));
    }
}