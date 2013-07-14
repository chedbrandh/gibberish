package com.chedbrandh.gibberish.exceptions;

import com.chedbrandh.gibberish.WordProvider;

/**
 * Exception indicating that a {@link WordProvider} does not have enough words.
 *
 * If a WordProvider is expected to have a bit coverage of n this means it
 * needs to have at least 2^n number of words.
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public class BitCoverageException extends Exception {

    private static final String MESSAGE_FORMAT =
            "Word provider '%s' does not provide the required bit coverage '%s'.";

    private final WordProvider wordProvider;
    private final int requiredBitCoverage;

    public BitCoverageException(WordProvider wordProvider,
                                int requiredBitCoverage) {
        super(String.format(MESSAGE_FORMAT, wordProvider.getName(), requiredBitCoverage));
        this.wordProvider = wordProvider;
        this.requiredBitCoverage = requiredBitCoverage;
    }

    public WordProvider getWordProvider() {
        return wordProvider;
    }

    public int getRequiredBitCoverage() {
        return requiredBitCoverage;
    }
}
