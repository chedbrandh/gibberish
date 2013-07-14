package com.chedbrandh.gibberish.exceptions;

import org.junit.Test;

import static com.chedbrandh.gibberish.PhraseTranslatorEndToEndTest.PROVIDER_1;
import static org.junit.Assert.assertEquals;

public class IllegalWordExceptionTest {

    @Test
    public void testGetters() throws Exception {
        IllegalWordException underTest = new IllegalWordException("foo", PROVIDER_1);
        assertEquals("foo", underTest.getWord());
        assertEquals(PROVIDER_1, underTest.getWordProvider());
    }
}
