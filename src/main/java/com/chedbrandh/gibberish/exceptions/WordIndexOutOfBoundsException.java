package com.chedbrandh.gibberish.exceptions;

import com.google.common.base.Preconditions;

import java.util.List;

/**
 * Exception indicating that a word maps to an index in a word provider
 * that is greater than what is allowed.
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public class WordIndexOutOfBoundsException extends Exception {

    private static final String MESSAGE_FORMAT =
            "Word '%s' at index '%s' is greater than what the number of bits (%s) allows for.";

    private final int index;
    private final String word;
    private final int numBits;

    /**
     * Private constructor used by verification method.
     *
     * @param index     Index to be verified.
     * @param word      Word that index maps to (used for exception handling).
     * @param numBits   Number of bits used for index translating.
     */
    private WordIndexOutOfBoundsException(int index, String word, int numBits) {
        super(String.format(MESSAGE_FORMAT, word, index, numBits));
        this.index = index;
        this.word = word;
        this.numBits = numBits;
    }

    public int getIndex() {
        return index;
    }

    public String getWord() {
        return word;
    }

    public int getNumBits() {
        return numBits;
    }

    /**
     * Verify that indices are within legal limits.
     *
     * If a word in a word provider maps to an index that is greater than what
     * is allowed, an exception is thrown. E.g. a word provider assigned
     * 4 bits is only allowed to map index 0 to 15 to a word, even if the
     * word provider has more than 16 words in it.
     *
     * @param indices           Indices to validate.
     * @param words             Words that indices map to (used for exception handling).
     * @param bitDistribution   Bit distribution used for translating indices.
     * @throws WordIndexOutOfBoundsException    If indices are illegal.
     */
    public static void verifyIndexLegality(List<Integer> indices,
                                           List<String> words,
                                           List<Integer> bitDistribution)
            throws WordIndexOutOfBoundsException {
        Preconditions.checkArgument(indices.size() == words.size(),
                "Number of indices must match the number of words.");
        Preconditions.checkArgument(indices.size() == bitDistribution.size(),
                "Number of indices must match the bit distribution.");
        for(int i = 0; i < indices.size(); i++) {
            int index = indices.get(i);
            String word = words.get(i);
            int numBits = bitDistribution.get(i);
            Preconditions.checkArgument(index >= 0, "Must provided non-negative index.");
            if (index >= Math.pow(2, numBits)) {
                throw new WordIndexOutOfBoundsException(index, word, numBits);
            }
        }
    }
}
