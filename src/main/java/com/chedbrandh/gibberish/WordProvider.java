package com.chedbrandh.gibberish;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.math.LongMath;

import java.math.RoundingMode;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * Class for providing an ordered set of legal words that does not contain
 * duplicates.
 *
 * Words are ordered so that the shortest and the words that lexicographically
 * compares the lowest, are first in the list.
 *
 * Also computes the mean word length for some set of words.
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public class WordProvider extends AbstractList<String> {

    // ordered list of unique words.
    private final ImmutableList<String> wordList;
    // provider reference name (used for exception handling).
    private final String name;

    /**
     * Creates a WordProvider given some words.
     *
     * Duplicate words in the provided word iterables are allowed.
     * @param words     An iterable of words used to create a WordProvider.
     * @param name      Provider reference name used in Schema (Used for
     *                  exception handling).
     */
    public WordProvider(Iterable<String> words, String name) {
        Set<String> wordSet = Sets.newHashSet(words);
        List<String> tempWordList = Lists.newArrayList(wordSet);
        sortLexicographicallyAndByLength(tempWordList);
        this.wordList = ImmutableList.copyOf(tempWordList);
        this.name = name;
    }

    /**
     * Get the mean word length for the numWords first words.
     *
     * @param numWords  Number of words to include in the calculation. Must be
     *                  a power of two.
     * @return          The mean word length.
     */
    public double meanWordLength(int numWords) {
        Preconditions.checkArgument(0 < numWords,
                "Must query mean word length for at least one word.");
        Preconditions.checkArgument(numWords <= size(),
                "Must query mean word length for at most all words.");
        Preconditions.checkArgument(LongMath.isPowerOfTwo(numWords),
                "Can only query for a number of words that is a power of two.");

        double totalLength = 0;
        for (int i = 0; i < numWords; i++) {
            totalLength += get(i).length();
        }
        return totalLength / numWords;
    }

    /**
     * Get word at a specific index.
     *
     * @param index Index of the location of the word.
     * @return      The word at index.
     */
    @Override
    public String get(int index) {
        return wordList.get(index);
    }

    /**
     * Returns the number of words in the ordered word list free from
     * duplicates.
     *
     * @return  Number of words given at construction without duplicates.
     */
    @Override
    public int size() {
        return wordList.size();
    }

    /**
     * Returns the log2 of the size() rounded down to the closest integer.
     *
     * With n number of bits there are 2^n possible combinations and thus 2^n
     * number of words can be generated. If the number of words available is
     * m the "bit coverge" is floor(log2(m))
     *
     * @return  The bit coverage.
     */
    public int bitCoverage() {
        return bitCoverage(size());
    }

    /**
     * Get the WordProvider reference name.
     *
     * @return  The name.
     */
    public String getName() {
        return name;
    }

    /**
     * First sort a list lexicographically and then by length.
     *
     * @param wordList  List of words to sort in-place.
     */
    @VisibleForTesting
    protected static void sortLexicographicallyAndByLength(List<String> wordList) {
        Collections.sort(wordList);
        Collections.sort(wordList, (s1, s2) -> s1.length() - s2.length());
    }

    /**
     * Returns the log2 of some number rounded down to the closest integer.
     *
     * @param number    Integer to get bit coverage for.
     * @return          The bit coverage.
     */
    private static int bitCoverage(int number) {
        return LongMath.log2(number, RoundingMode.DOWN);
    }
}
