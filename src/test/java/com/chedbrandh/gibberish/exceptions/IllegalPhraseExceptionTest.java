package com.chedbrandh.gibberish.exceptions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IllegalPhraseExceptionTest {

    @Test
    public void testConstructor() throws Exception {
        IllegalPhraseException underTest =
                new IllegalPhraseException("foo phrase", "foo message");
        assertEquals("foo phrase", underTest.getPhrase());
        assertEquals("foo message", underTest.getMessage());
    }

    @Test
    public void testExpectedLeading() throws Exception {
        IllegalPhraseException underTest =
                IllegalPhraseException.expectedLeading("foo phrase", "");
        assertEquals("foo phrase", underTest.getPhrase());
    }

    @Test
    public void testExpectedTrailing() throws Exception {
        IllegalPhraseException underTest =
                IllegalPhraseException.expectedTrailing("foo phrase", "");
        assertEquals("foo phrase", underTest.getPhrase());
    }

    @Test
    public void testExpectedSeparator() throws Exception {
        IllegalPhraseException underTest =
                IllegalPhraseException.expectedSeparator("foo phrase", "");
        assertEquals("foo phrase", underTest.getPhrase());
    }
}
