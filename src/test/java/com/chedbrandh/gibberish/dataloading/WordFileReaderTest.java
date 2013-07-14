package com.chedbrandh.gibberish.dataloading;

import com.google.common.collect.Sets;
import com.chedbrandh.gibberish.exceptions.DuplicateWordException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class WordFileReaderTest {

    @Test
    public void testHappyPath() throws Exception {
        String str = "apa\nbpa\ncpa";
        InputStream is = new ByteArrayInputStream(str.getBytes());
        WordFileReader wordFileReader = new WordFileReader(is, "");
        assertEquals(Sets.newHashSet("apa", "bpa", "cpa"), wordFileReader.getWordSet());
    }

    @Test
    public void testNonAlphaNumeric() throws Exception {
        String str = "%s\nfoo\nbar\n(.*|baz$)\ns p a c e";
        InputStream is = new ByteArrayInputStream(str.getBytes());
        WordFileReader wordFileReader = new WordFileReader(is, "");
        assertEquals(Sets.newHashSet("%s", "foo", "bar", "(.*|baz$)", "s p a c e"), wordFileReader.getWordSet());
    }

    @Test
    public void testDuplicates() throws Exception {
        String str = "apa\napa\napa";
        InputStream is = new ByteArrayInputStream(str.getBytes());
        try {
            new WordFileReader(is, "foo file");
            throw new RuntimeException("Expected exception was not thrown.");
        } catch (DuplicateWordException e) {
            assertEquals("foo file", e.getFileReference());
            assertEquals("apa", e.getWord());
        }
    }
}
