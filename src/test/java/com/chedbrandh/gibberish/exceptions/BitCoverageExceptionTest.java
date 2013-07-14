package com.chedbrandh.gibberish.exceptions;

import org.junit.Test;

import static com.chedbrandh.gibberish.PhraseTranslatorEndToEndTest.PROVIDER_1;
import static org.junit.Assert.assertEquals;

public class BitCoverageExceptionTest {

    @Test
    public void testGetters() throws Exception {
        BitCoverageException underTest = new BitCoverageException(PROVIDER_1, 2);
        assertEquals(PROVIDER_1, underTest.getWordProvider());
        assertEquals(2, underTest.getRequiredBitCoverage());
    }
}
