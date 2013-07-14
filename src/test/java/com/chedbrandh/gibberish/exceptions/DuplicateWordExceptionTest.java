package com.chedbrandh.gibberish.exceptions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DuplicateWordExceptionTest {

    @Test
    public void testGetters() throws Exception {
        DuplicateWordException underTest = new DuplicateWordException("foo", "bar");
        assertEquals("foo", underTest.getFileReference());
        assertEquals("bar", underTest.getWord());
    }
}
