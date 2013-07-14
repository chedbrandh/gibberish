package com.chedbrandh.gibberish;

import com.chedbrandh.gibberish.exceptions.WordIndexOutOfBoundsException;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static com.chedbrandh.gibberish.PhraseTranslatorEndToEndTest.intsToBytes;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class PhraseTranslatorTest {

    private static final int BIT_COVERAGE = 5;
    private static final List<Integer> BIT_DISTRIBUTION = ImmutableList.of(2, 3);
    private static final byte[] BYTES = intsToBytes(5);
    private static final int FROM_BIT_INDEX = 1;
    private static final int TO_BIT_INDEX = 3;
    private static final List<Integer> INDICES = ImmutableList.of(1, 2);
    private static final List<String> WORDS = ImmutableList.of("foo", "bar");
    private static final String PHRASE = "apabpacpa";

    @Mock private WordProviderSequence wordProviderSequence;
    @Mock private IndexTranslator indexTranslator;
    @Mock private IPhraseConstructor phraseConstructor;
    @Mock private IPhraseDeconstructor phraseDeconstructor;

    private PhraseTranslator undertest;

    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);

        // mock out mocks
        when(indexTranslator.bitCoverage()).thenReturn(BIT_COVERAGE);
        when(indexTranslator.bitDistribution()).thenReturn(BIT_DISTRIBUTION);
        when(indexTranslator.fromBytes(BYTES, FROM_BIT_INDEX, TO_BIT_INDEX)).thenReturn(INDICES);
        when(indexTranslator.fromBytes(BYTES, 0, BIT_COVERAGE)).thenReturn(INDICES);

        when(wordProviderSequence.getWords(INDICES)).thenReturn(WORDS);
        when(wordProviderSequence.getIndices(WORDS)).thenReturn(INDICES);

        when(phraseConstructor.construct(WORDS)).thenReturn(PHRASE);
        when(phraseDeconstructor.deconstruct(PHRASE)).thenReturn(WORDS);

        // construct translator to test
        undertest = new PhraseTranslator(wordProviderSequence, indexTranslator,
                phraseConstructor, phraseDeconstructor);

        // verify calls to mocks in constructor
        verify(indexTranslator).bitCoverage();
        verify(indexTranslator).bitDistribution();
        verify(wordProviderSequence).verifyProviderBitCoverage(BIT_DISTRIBUTION);
        verifyNoMoreInteractions(indexTranslator, wordProviderSequence, phraseConstructor);
    }

    @Test
    public void testFromBytes() throws Exception {
        assertEquals(PHRASE, undertest.fromBytes(BYTES, FROM_BIT_INDEX, TO_BIT_INDEX));
        verify(indexTranslator).fromBytes(BYTES, FROM_BIT_INDEX, TO_BIT_INDEX);
        verify(wordProviderSequence).getWords(INDICES);
        verify(phraseConstructor).construct(WORDS);
        verifyNoMoreInteractions(indexTranslator, wordProviderSequence, phraseConstructor);
    }

    @Test
    public void testToBytes() throws Exception {
        undertest.toBytes(BYTES, PHRASE, FROM_BIT_INDEX, TO_BIT_INDEX);
        verify(phraseDeconstructor).deconstruct(PHRASE);
        verify(wordProviderSequence).getIndices(WORDS);
        verify(indexTranslator, times(2)).bitDistribution();
        verify(indexTranslator).toBytes(BYTES, INDICES, FROM_BIT_INDEX, TO_BIT_INDEX);
        verifyNoMoreInteractions(indexTranslator, wordProviderSequence, phraseConstructor);
    }

    @Test
    public void testFromLong() throws Exception {
        assertEquals(PHRASE, undertest.fromLong(BYTES[0]));
        verify(indexTranslator).fromBytes(BYTES, 0, BIT_COVERAGE);
        verify(wordProviderSequence).getWords(INDICES);
        verify(phraseConstructor).construct(WORDS);
        verifyNoMoreInteractions(indexTranslator, wordProviderSequence, phraseConstructor);
    }

    @Test
    public void testToLong() throws Exception {
        undertest.toLong(PHRASE);
        verify(phraseDeconstructor).deconstruct(PHRASE);
        verify(wordProviderSequence).getIndices(WORDS);
        verify(indexTranslator, times(2)).bitDistribution();
        // since mock indexTranslator does not mutate byte array it will always be zero
        verify(indexTranslator).toBytes(new byte[] {0}, INDICES, 0, BIT_COVERAGE);
        verifyNoMoreInteractions(indexTranslator, wordProviderSequence, phraseConstructor);
    }

    @Test
    public void testLongToBytes() throws Exception {
        assertArrayEquals(new byte[] {7}, PhraseTranslator.longToBytes(7, 8));
        assertArrayEquals(new byte[] {7, 0}, PhraseTranslator.longToBytes(7, 9));
        assertArrayEquals(new byte[] {7, 0}, PhraseTranslator.longToBytes(7, 16));
        assertArrayEquals(new byte[] {3}, PhraseTranslator.longToBytes(7, 2));
        assertArrayEquals(new byte[] {-1, -1, 1}, PhraseTranslator.longToBytes(0xFFFFFF, 17));
    }

    @Test
    public void testBytesToLong() throws Exception {
        assertEquals(7, PhraseTranslator.bytesToLong(new byte[] {7}, 8));
        assertEquals(1, PhraseTranslator.bytesToLong(new byte[]{7, 0}, 1));
        assertEquals(7, PhraseTranslator.bytesToLong(new byte[]{7, 0}, 16));
        assertEquals(0b111111111, PhraseTranslator.bytesToLong(new byte[]{-1, -1}, 9));
    }

    @Test
    public void testNumBitsToNumBytes() throws Exception {
        assertEquals(0, PhraseTranslator.numBitsToNumBytes(0));
        assertEquals(1, PhraseTranslator.numBitsToNumBytes(1));
        assertEquals(1, PhraseTranslator.numBitsToNumBytes(8));
        assertEquals(2, PhraseTranslator.numBitsToNumBytes(9));
        assertEquals(42, PhraseTranslator.numBitsToNumBytes(Byte.SIZE * 42));
        assertEquals(42 + 1, PhraseTranslator.numBitsToNumBytes(Byte.SIZE * 42 + 1));
    }

    @Test
    public void testGetIndexTranslator() throws Exception {
        assertEquals(indexTranslator, undertest.getIndexTranslator());
    }

    @Test
    public void testGetWordProviderSequence() throws Exception {
        assertEquals(wordProviderSequence, undertest.getWordProviderSequence());
    }

    @Test(expected = WordIndexOutOfBoundsException.class)
    public void testToBytesWordIndexOutOfBounds() throws Exception {
        List<Integer> bitDistribution = ImmutableList.of(2, 1);
        when(indexTranslator.bitDistribution()).thenReturn(bitDistribution);
        undertest.toBytes(BYTES, PHRASE, FROM_BIT_INDEX, TO_BIT_INDEX);
    }
}
