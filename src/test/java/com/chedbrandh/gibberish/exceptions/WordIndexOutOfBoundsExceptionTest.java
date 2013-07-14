package com.chedbrandh.gibberish.exceptions;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class WordIndexOutOfBoundsExceptionTest {

    @Test
    public void testVerifySuccess() throws Exception {
        List<Integer> indices = ImmutableList.of(1, 3);
        List<String> words = ImmutableList.of("foo", "bar");
        List<Integer> bitDistribution = ImmutableList.of(3, 2);
        WordIndexOutOfBoundsException.verifyIndexLegality(indices, words, bitDistribution);
    }

    @Test
    public void testVerifyFail() throws Exception {
        List<Integer> indices = ImmutableList.of(1, 4);
        List<String> words = ImmutableList.of("foo", "bar");
        List<Integer> bitDistribution = ImmutableList.of(3, 2);
        try {
            WordIndexOutOfBoundsException.verifyIndexLegality(indices, words, bitDistribution);
            throw new RuntimeException("Did not throw expected WordIndexOutOfBoundsException");
        } catch (WordIndexOutOfBoundsException e) {
            assertEquals(4, e.getIndex());
            assertEquals("bar", e.getWord());
            assertEquals(2, e.getNumBits());
        }
    }
}
