package com.chedbrandh.gibberish.dataloading;

import com.google.common.collect.ImmutableSet;
import com.chedbrandh.gibberish.exceptions.DuplicateWordException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for reading word files.
 *
 * The new line character acts as delimiter for the words. I.e. each line is
 * read as a word. All characters (except new line) are allowed including
 * white spaces and other non-alphanumeric characters.
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public class WordFileReader {

    private final ImmutableSet<String> words;

    /**
     * Read word file from input stream.
     *
     * @param inputStream               InputStream to read words from.
     * @param fileReference             File reference name used in Schema (Used
     *                                  for exception handling).
     * @throws IOException              Thrown if problems reading word file.
     * @throws DuplicateWordException   Thrown if word file contains duplicate words.
     */
    public WordFileReader(InputStream inputStream, String fileReference)
            throws IOException, DuplicateWordException {
        Set<String> wordSet = new HashSet<>();
        try (BufferedReader input = new BufferedReader(new InputStreamReader(inputStream))) {
            String word;
            while ((word = input.readLine()) != null) {
                if (wordSet.contains(word)) {
                    throw new DuplicateWordException(fileReference, word);
                }
                wordSet.add(word);
            }
        } finally {
            words = ImmutableSet.copyOf(wordSet);
        }
    }

    /**
     * Get the set of words in the file.
     *
     * @return Returns the set of words.
     */
    public Set<String> getWordSet() {
        return words;
    }
}
