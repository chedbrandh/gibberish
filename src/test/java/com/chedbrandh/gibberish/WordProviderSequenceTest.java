package com.chedbrandh.gibberish;

import com.chedbrandh.gibberish.exceptions.IllegalWordException;
import com.google.common.collect.ImmutableList;
import com.chedbrandh.gibberish.exceptions.BitCoverageException;
import org.junit.Test;

import java.util.List;

import static com.chedbrandh.gibberish.PhraseTranslatorEndToEndTest.PROVIDER_1;
import static com.chedbrandh.gibberish.PhraseTranslatorEndToEndTest.PROVIDER_2;
import static org.junit.Assert.assertEquals;

public class WordProviderSequenceTest {

    private static final List<WordProvider> PROVIDERS_1 = ImmutableList.of(PROVIDER_1, PROVIDER_2);

    @Test
    public void testGetWordList() throws Exception {
        WordProviderSequence undertest = new WordProviderSequence(PROVIDERS_1);
        assertEquals(ImmutableList.of("1", "a"), undertest.getWords(ImmutableList.of(0, 0)));
        assertEquals(ImmutableList.of("5", "c"), undertest.getWords(ImmutableList.of(4, 2)));
    }

    @Test
    public void testGetIndices() throws Exception {
        WordProviderSequence undertest = new WordProviderSequence(PROVIDERS_1);
        assertEquals(ImmutableList.of(0, 0), undertest.getIndices(ImmutableList.of("1", "a")));
        assertEquals(ImmutableList.of(4, 2), undertest.getIndices(ImmutableList.of("5", "c")));
    }

    @Test
    public void testGetIndicesIllegalWord() throws Exception {
        WordProviderSequence undertest = new WordProviderSequence(PROVIDERS_1);
        try {
            undertest.getIndices(ImmutableList.of("1", "z"));
            throw new RuntimeException("Expected exception was not thrown.");
        } catch (IllegalWordException e) {
            assertEquals("z", e.getWord());
            assertEquals("foo", e.getWordProvider().getName());
        }
    }

    @Test
    public void testBitCoverage() throws Exception {
        WordProviderSequence undertest = new WordProviderSequence(PROVIDERS_1);
        assertEquals(3, undertest.bitCoverage());
    }

    @Test
    public void testVerifyProviderBitCoverageSuccess() throws Exception {
        WordProviderSequence undertest = new WordProviderSequence(PROVIDERS_1);
        undertest.verifyProviderBitCoverage(ImmutableList.of(2, 1));
    }

    @Test
    public void testVerifyProviderBitCoverageFailure() throws Exception {
        WordProviderSequence undertest = new WordProviderSequence(PROVIDERS_1);

        try {
            undertest.verifyProviderBitCoverage(ImmutableList.of(3, 1));
            throw new RuntimeException("Verification did not fail as expected.");
        } catch(BitCoverageException e) {
            assertEquals(PROVIDER_1.getName(), e.getWordProvider().getName());
            assertEquals(3, e.getRequiredBitCoverage());
        }
    }

    @Test
    public void testComputeChecksum() throws Exception {
        String expectedChecksum = "00ee10f582054a728d389714aee2e7222cb9e89c";
        List<WordProvider> providers = ImmutableList.of(
                new WordProvider(ImmutableList.of("1", "22"), "foo"),
                new WordProvider(ImmutableList.of("4444", "333"), "foo"));
        WordProviderSequence undertest = new WordProviderSequence(providers);
        assertEquals(expectedChecksum, undertest.computeChecksum());
    }

    @Test
    public void testComputeChecksumWordOrderIndifferent() throws Exception {
        List<WordProvider> providers1 =
                ImmutableList.of(new WordProvider(ImmutableList.of("1", "22"), "foo"));
        List<WordProvider> providers2 =
                ImmutableList.of(new WordProvider(ImmutableList.of("22", "1"), "foo"));

        WordProviderSequence undertest1 = new WordProviderSequence(providers1);
        WordProviderSequence undertest2 = new WordProviderSequence(providers2);

        assertEquals(undertest1.computeChecksum(), undertest2.computeChecksum());
    }

}
