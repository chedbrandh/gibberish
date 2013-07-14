package com.chedbrandh.gibberish;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class WordProviderTest {

    private static final Iterable<String> WORDS_1 = ImmutableList.of("apa", "bpa");
    private static final Iterable<String> WORDS_2 = ImmutableList.of("foo", "bar");
    private static final Iterable<String> WORDS_3 = ImmutableList.of("apa", "foo");
    private static final Iterable<String> WORDS_4 = ImmutableList.of("1", "a", "b", "abc", "xyz");
    private static final Iterable<String> WORDS_5 = ImmutableList.of("%s", "foo bar", "(.*|baz$)");

    @Test
    public void testGetWord() throws Exception {
        assertEquals("1", new WordProvider(Iterables.concat(WORDS_1, WORDS_4), "").get(0));
        assertEquals("a", new WordProvider(Iterables.concat(WORDS_1, WORDS_4), "").get(1));
        assertEquals("b", new WordProvider(Iterables.concat(WORDS_1, WORDS_4), "").get(2));
        assertEquals("abc", new WordProvider(Iterables.concat(WORDS_1, WORDS_4), "").get(3));
        assertEquals("apa", new WordProvider(Iterables.concat(WORDS_1, WORDS_4), "").get(4));
        assertEquals("bpa", new WordProvider(Iterables.concat(WORDS_1, WORDS_4), "").get(5));
        assertEquals("xyz", new WordProvider(Iterables.concat(WORDS_1, WORDS_4), "").get(6));
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals("foo", new WordProvider(WORDS_1, "foo").getName());
    }

    @Test
    public void testConstructorNonAlphaNumeric() throws Exception {
        new WordProvider(WORDS_5, "");
    }

    @Test
    public void testGetIndex() throws Exception {
        assertEquals(0, new WordProvider(WORDS_4, "").indexOf("1"));
        assertEquals(1, new WordProvider(WORDS_4, "").indexOf("a"));
        assertEquals(2, new WordProvider(WORDS_4, "").indexOf("b"));
        assertEquals(4, new WordProvider(WORDS_4, "").indexOf("xyz"));
    }

    @Test
    public void testGetIndexFailNotFound() throws Exception {
        assertEquals(-1, new WordProvider(WORDS_4, "").indexOf("blargh"));
    }

    @Test
    public void testMeanWordLength() throws Exception {
        Iterable<String> words = ImmutableSet.of("a", "ab", "abc", "abcd", "abcde");
        assertEquals(3.0, new WordProvider(WORDS_1, "").meanWordLength(1), 0.0);
        assertEquals(3.0, new WordProvider(WORDS_1, "").meanWordLength(2), 0.0);
        assertEquals(1.0, new WordProvider(WORDS_4, "").meanWordLength(1), 0.0);
        assertEquals(1.0, new WordProvider(WORDS_4, "").meanWordLength(2), 0.0);
        assertEquals(1.5, new WordProvider(WORDS_4, "").meanWordLength(4), 0.0);
        assertEquals(1.0, new WordProvider(words, "").meanWordLength(1), 0.0);
        assertEquals(1.5, new WordProvider(words, "").meanWordLength(2), 0.0);
        assertEquals(2.5, new WordProvider(words, "").meanWordLength(4), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMeanWordLengthFailZero() throws Exception {
        new WordProvider(WORDS_1, "").meanWordLength(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMeanWordLengthFailTooBig() throws Exception {
        new WordProvider(WORDS_1, "").meanWordLength(100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMeanWordLengthFailNoPowerOfTwo() throws Exception {
        new WordProvider(WORDS_4, "").meanWordLength(3);
    }

    @Test
    public void testSize() throws Exception {
        assertEquals(2, new WordProvider(WORDS_1, "").size());
        assertEquals(3, new WordProvider(Iterables.concat(WORDS_1, WORDS_3), "").size());
        assertEquals(4, new WordProvider(Iterables.concat(WORDS_1, WORDS_2), "").size());
    }

    @Test
    public void testBitCoverage() throws Exception {
        ImmutableList<String> words = ImmutableList.of("1", "2", "3", "4", "5");
        assertEquals(0, new WordProvider(words.subList(0, 1), "").bitCoverage());
        assertEquals(1, new WordProvider(words.subList(0, 2), "").bitCoverage());
        assertEquals(1, new WordProvider(words.subList(0, 3), "").bitCoverage());
        assertEquals(2, new WordProvider(words.subList(0, 4), "").bitCoverage());
        assertEquals(2, new WordProvider(words.subList(0, 5), "").bitCoverage());
    }

    @Test
    public void testSortLexicographicallyAndByLength() throws Exception {
        List<String> list = Lists.newArrayList("xyz", "a", "b", "abc", "a", "1");
        List<String> expected = ImmutableList.of("1", "a", "a", "b", "abc", "xyz");
        WordProvider.sortLexicographicallyAndByLength(list);
        assertEquals(expected, list);
    }
}
