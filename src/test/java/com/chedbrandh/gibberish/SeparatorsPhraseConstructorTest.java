package com.chedbrandh.gibberish;

import com.google.common.collect.ImmutableList;
import com.chedbrandh.gibberish.exceptions.IllegalPhraseException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class SeparatorsPhraseConstructorTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNoSeparators() throws Exception {
        new SeparatorsPhraseConstructor(ImmutableList.of("foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptySeparators() throws Exception {
        new SeparatorsPhraseConstructor(ImmutableList.of("", "", ""));
    }

    @Test
    public void testRegexStrings() throws Exception {
        List<String> separators = ImmutableList.of("", ".*", "%s");
        List<String> words = ImmutableList.of("%d", "(\\/|-|\\.)");
        SeparatorsPhraseConstructor undertest = new SeparatorsPhraseConstructor(separators);
        String actualPhrase = undertest.construct(words);
        String expectedPhrase = separators.get(0) + words.get(0) +
                separators.get(1) + words.get(1) + separators.get(2);
        assertEquals(expectedPhrase, actualPhrase);
        assertEquals(words, undertest.deconstruct(actualPhrase));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructWrongNumberOfWords() throws Exception {
        List<String> separators = ImmutableList.of("foo", "bar", "baz");
        List<String> words = ImmutableList.of("apa", "bpa", "cpa");
        SeparatorsPhraseConstructor undertest = new SeparatorsPhraseConstructor(separators);
        undertest.construct(words);
    }

    @Test(expected = IllegalPhraseException.class)
    public void testDeconstructMissingPrefix() throws Exception {
        List<String> separators = ImmutableList.of("foo", "bar", "baz");
        SeparatorsPhraseConstructor undertest = new SeparatorsPhraseConstructor(separators);
        undertest.deconstruct("f123bar234baz");
    }

    @Test(expected = IllegalPhraseException.class)
    public void testDeconstructMissingSuffix() throws Exception {
        List<String> separators = ImmutableList.of("foo", "bar", "baz");
        SeparatorsPhraseConstructor undertest = new SeparatorsPhraseConstructor(separators);
        undertest.deconstruct("foo123bar234ba");
    }

    @Test(expected = IllegalPhraseException.class)
    public void testDeconstructMissingSeparator() throws Exception {
        List<String> separators = ImmutableList.of("foo", "bar", "baz");
        SeparatorsPhraseConstructor undertest = new SeparatorsPhraseConstructor(separators);
        undertest.deconstruct("foo123br234baz");
    }
}
