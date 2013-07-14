package com.chedbrandh.gibberish.exceptions;

import com.chedbrandh.gibberish.WordProvider;

/**
 * Exception indicating that a word is unknown by the
 * {@link com.chedbrandh.gibberish.PhraseTranslator}.
 *
 * When a phrase translation is attempted but one of the words in the phrase
 * does not exist in the corresponding {@link WordProvider} in the
 * {@link com.chedbrandh.gibberish.PhraseTranslator} used for translation
 * this exception should be thrown.
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public class IllegalWordException extends Exception {

    private static final String MESSAGE_FORMAT =
            "Could not find word '%s' in word provider '%s'.";

    private final String word;
    private final WordProvider wordProvider;

    public IllegalWordException(String word, WordProvider wordProvider) {
        super(String.format(MESSAGE_FORMAT, word, wordProvider.getName()));
        this.word = word;
        this.wordProvider = wordProvider;
    }

    public String getWord() {
        return word;
    }

    public WordProvider getWordProvider() {
        return wordProvider;
    }
}
