package com.chedbrandh.gibberish.exceptions;

/**
 * Exception indicating that a word file contains duplicate words.
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public class DuplicateWordException extends Exception {

    private static final String MESSAGE_FORMAT = "File '%s' contains duplicate words. See '%s'.";

    private final String fileReference;
    private final String word;

    public DuplicateWordException(String fileReference, String word) {
        super(String.format(MESSAGE_FORMAT, fileReference, word));
        this.fileReference = fileReference;
        this.word = word;
    }

    public String getFileReference() {
        return fileReference;
    }

    public String getWord() {
        return word;
    }
}
