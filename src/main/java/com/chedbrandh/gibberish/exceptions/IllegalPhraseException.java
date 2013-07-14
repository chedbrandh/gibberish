package com.chedbrandh.gibberish.exceptions;

/**
 * Exception indicating that a phrase is on the wrong format.
 *
 * When a phrase translation is attempted but the phrase is not on a format
 * that the {@link com.chedbrandh.gibberish.PhraseTranslator} is expecting
 * this exception should be thrown.
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public class IllegalPhraseException extends Exception {

    private static final String EXPECTED_LEADING_FORMAT =
            "Could not find expected leading substring '%s' in phrase '%s'.";
    private static final String EXPECTED_TRAILING_FORMAT =
            "Could not find expected trailing substring '%s' in phrase '%s'.";
    private static final String EXPECTED_SEPARATOR_FORMAT =
            "Could not find expected separator '%s' in phrase '%s'.";

    private final String phrase;

    public IllegalPhraseException(String phrase, String message) {
        super(message);
        this.phrase = phrase;
    }

    public static IllegalPhraseException expectedLeading(
            String phrase, String leadingSubstring) {
        return new IllegalPhraseException(phrase,
                String.format(EXPECTED_LEADING_FORMAT, leadingSubstring, phrase));
    }

    public static IllegalPhraseException expectedTrailing(
            String phrase, String trailingSubstring) {
        return new IllegalPhraseException(phrase,
                String.format(EXPECTED_TRAILING_FORMAT, trailingSubstring, phrase));
    }

    public static IllegalPhraseException expectedSeparator(
            String phrase, String separator) {
        return new IllegalPhraseException(phrase,
                String.format(EXPECTED_SEPARATOR_FORMAT, separator, phrase));
    }

    public String getPhrase() {
        return phrase;
    }
}
