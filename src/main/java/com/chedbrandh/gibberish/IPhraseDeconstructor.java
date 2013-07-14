package com.chedbrandh.gibberish;

import com.chedbrandh.gibberish.exceptions.IllegalPhraseException;

import java.util.List;

/**
 * Interface for deconstructing words from phrases.
 *
 * Words here actually mean any {@link String}, even ones that include
 * non-alphanumeric characters.
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public interface IPhraseDeconstructor {
    List<String> deconstruct(String phrase) throws IllegalPhraseException;
}
