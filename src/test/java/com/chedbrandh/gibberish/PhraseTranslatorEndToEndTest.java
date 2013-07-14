package com.chedbrandh.gibberish;

import com.chedbrandh.gibberish.dataloading.WordFileReader;
import com.chedbrandh.gibberish.exceptions.BitCoverageException;
import com.chedbrandh.gibberish.exceptions.IllegalPhraseException;
import com.chedbrandh.gibberish.exceptions.IllegalWordException;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Note that all bit sequences, between some start and end bit index, are
 * written and read as little-endian representations of some value.
 */
public class PhraseTranslatorEndToEndTest {

    public static final WordProvider PROVIDER_1 =
            new WordProvider(ImmutableList.of("1", "2", "3", "4", "5"), "");
    public static final WordProvider PROVIDER_2 =
            new WordProvider(ImmutableList.of("a", "b", "c"), "foo");

    private static final byte[] TEST_BYTES = intsToBytes(
            0x00, 0x01, 0x08, 0x10, 0x80, 0x99, 0xFF, 0x46,
            0x0a, 0xdb, 0xad, 0xdf, 0x2b, 0x7b, 0x9f, 0x58);

    private static final List<Long> TEST_LONGS = ImmutableList.of(
            0L, 1L, 2L, 4235509150918226520L, 6021426017937024543L, 7567446619331070120L,
            2880894691774343537L, 7554473703927744198L, 3821048721851812186L, 1264428019640522977L,
            8362440238844169579L, 1124491381495416527L, 1386819188864531693L);

    private static final WordProviderSequence PROVIDER_SEQUENCE =
            new WordProviderSequence(ImmutableList.of(PROVIDER_1, PROVIDER_2));

    private static final SeparatorsPhraseConstructor PHRASE_CONSTRUCTOR =
            new SeparatorsPhraseConstructor(ImmutableList.of("apa", "bpa", "cpa", "dpa", "epa"));
    private static final SeparatorsPhraseConstructor PHRASE_CONSTRUCTOR_2 =
            new SeparatorsPhraseConstructor(ImmutableList.of("", " ", ""));

    private static final IndexTranslator INDEX_TRANSLATOR =
            new IndexTranslator(ImmutableList.of(4, 4, 4, 4));
    private static final IndexTranslator INDEX_TRANSLATOR_2 =
            new IndexTranslator(ImmutableList.of(2, 1));

    private PhraseTranslator undertest;
    private PhraseTranslator undertest2;


    @Before
    public void before() throws Exception {

        InputStream lettersInputStream =
                ClassLoader.getSystemResourceAsStream("word_file_letters.txt");
        InputStream numbersInputStream =
                ClassLoader.getSystemResourceAsStream("word_file_numbers.txt");

        WordProvider providerLetters = new WordProvider(
                new WordFileReader(lettersInputStream, "").getWordSet(), "");
        WordProvider providerNumbers = new WordProvider(
                new WordFileReader(numbersInputStream, "").getWordSet(), "");

        WordProviderSequence providerSequence = new WordProviderSequence(ImmutableList.of(
                providerLetters, providerNumbers, providerLetters, providerNumbers));

        undertest = new PhraseTranslator(
                providerSequence, INDEX_TRANSLATOR, PHRASE_CONSTRUCTOR, PHRASE_CONSTRUCTOR);

        undertest2 = new PhraseTranslator(
                PROVIDER_SEQUENCE, INDEX_TRANSLATOR_2, PHRASE_CONSTRUCTOR_2, PHRASE_CONSTRUCTOR_2);
    }

    @Test
    public void testBitCoverageFailure() throws Exception {
        IndexTranslator indexTranslator = new IndexTranslator(ImmutableList.of(2, 2));
        try {
            new PhraseTranslator(PROVIDER_SEQUENCE, indexTranslator,
                    PHRASE_CONSTRUCTOR_2, PHRASE_CONSTRUCTOR_2);
            throw new RuntimeException("Did not throw expected BitCoverageException");
        } catch (BitCoverageException e) {
            Assert.assertEquals(PROVIDER_2.getName(), e.getWordProvider().getName());
            assertEquals(2, e.getRequiredBitCoverage());
        }
    }

    @Test
    public void testTwoWayTranslationTwoBytes() throws Exception {
        for (int i = 0; i + 1 < TEST_BYTES.length; i++) {
            byte[] originalBytes = new byte[] {TEST_BYTES[i], TEST_BYTES[i + 1]};
            String originalPhrase = undertest.fromBytes(originalBytes, 0, 16);
            byte[] translatedBytes = new byte[2];
            undertest.toBytes(translatedBytes, originalPhrase, 0, 16);
            assertTrue(Arrays.equals(originalBytes, translatedBytes));
            String translatedPhrase = undertest.fromBytes(translatedBytes, 0, 16);
            assertEquals(originalPhrase, translatedPhrase);
        }
    }

    @Test
    public void testFromBytes() throws Exception {
        // 0->0b00000000->0b000 => 0b00->0->"1", 0b0->0->"a"
        assertEquals("1 a", undertest2.fromBytes(intsToBytes(0), 0, 3));
        // 16->0b00001000->0b001 => 0b00->0->"1", 0b1->1->"b"
        assertEquals("1 b", undertest2.fromBytes(intsToBytes(16), 2, 5));
        // 8->0b00010000->0b100 => 0b10->1->"2", 0b0->0->"a"
        assertEquals("2 a", undertest2.fromBytes(intsToBytes(8), 3, 6));
        // 32->0b00000100->0b010 => 0b01->2->"3", 0b0->0->"a"
        assertEquals("3 a", undertest2.fromBytes(intsToBytes(32), 4, 7));
        // 8->0b00010000->0b000 => 0b00->0->"1", 0b0->0->"a"
        assertEquals("1 a", undertest2.fromBytes(intsToBytes(8), 0, 3));
        // 255->0b11111111->0b111 => 0b11->3->"4", 0b1->1->"b"
        assertEquals("4 b", undertest2.fromBytes(intsToBytes(255), 0, 3));
    }

    @Test
    public void testToBytes() throws Exception {
        byte[] bytes;
        bytes = new byte[1];
        // "4"->3->0b11, "b"->1->0b1 => 0b111->0b01110000->(14)
        undertest2.toBytes(bytes, "4 b", 1, 4);
        assertTrue(Arrays.equals(intsToBytes(14), bytes));
        bytes = new byte[2];
        // "3"->2->0b01, "b"->1->0b1 => 0b011->0b00000001 0b10000000->(128,1)
        undertest2.toBytes(bytes, "3 b", 6, 9);
        assertTrue(Arrays.equals(intsToBytes(128, 1), bytes));
    }

    @Test
    public void testFromLong() throws Exception {
        // 0b000 => 0b00->0->"1", 0b0->0->"a"
        assertEquals("1 a", undertest2.fromLong(0));
        // 0b100 => 0b10->1->"2", 0b0->0->"a"
        assertEquals("2 a", undertest2.fromLong(1));
        // 0b010 => 0b01->2->"3", 0b0->0->"a"
        assertEquals("3 a", undertest2.fromLong(2));
        // 0b110 => 0b11->3->"4", 0b0->0->"a"
        assertEquals("4 a", undertest2.fromLong(3));
        // 0b001 => 0b00->0->"1", 0b1->1->"b"
        assertEquals("1 b", undertest2.fromLong(4));
        // 0b101 => 0b10->1->"2", 0b1->1->"b"
        assertEquals("2 b", undertest2.fromLong(5));
        // 0b011 => 0b01->2->"3", 0b1->1->"b"
        assertEquals("3 b", undertest2.fromLong(6));
        // 0b111 => 0b11->3->"4", 0b1->1->"b"
        assertEquals("4 b", undertest2.fromLong(7));
        // 255l->0b11111111->0b111 => 0b11->3->"4", 0b1->1->"b"
        assertEquals("4 b", undertest2.fromLong(255));
    }

    @Test
    public void testToLong() throws Exception {
        // "1"->0->0b00, "a"->0->0b0 => 0b000->0l
        assertEquals(0, undertest2.toLong("1 a"));
        // "2"->1->0b10, "a"->0->0b0 => 0b100->1l
        assertEquals(1, undertest2.toLong("2 a"));
        // "3"->2->0b01, "a"->0->0b0 => 0b010->2l
        assertEquals(2, undertest2.toLong("3 a"));
        // "4"->3->0b11, "a"->0->0b0 => 0b110->3l
        assertEquals(3, undertest2.toLong("4 a"));
        // "1"->0->0b00, "b"->1->0b1 => 0b001->4l
        assertEquals(4, undertest2.toLong("1 b"));
        // "2"->1->0b10, "b"->1->0b1 => 0b101->5l
        assertEquals(5, undertest2.toLong("2 b"));
        // "3"->2->0b01, "b"->1->0b1 => 0b011->6l
        assertEquals(6, undertest2.toLong("3 b"));
        // "4"->3->0b11, "b"->1->0b1 => 0b111->7l
        assertEquals(7, undertest2.toLong("4 b"));
    }

    @Test(expected = IllegalPhraseException.class)
    public void testToLongIllegalPhrase() throws Exception {
        undertest2.toLong("foo");
    }

    @Test(expected = IllegalWordException.class)
    public void testToLongIllegalWord() throws Exception {
        undertest2.toLong("9 z");
    }

    @Test
    public void testTwoWayTranslationLongs() throws Exception {
        long mask = 65535L;
        for (long l : TEST_LONGS) {
            assertEquals(mask & l, undertest.toLong(undertest.fromLong(l)));
        }
    }

    public static byte[] intsToBytes(Integer... ints) {
        byte[] result = new byte[ints.length];
        for (int i = 0; i < ints.length; i++) {
            result[i] = (byte)(int)ints[i];
        }
        return result;
    }
}
