package com.chedbrandh.gibberish;

import java.util.List;

/**
 * Interface for constructing phrases from words.
 *
 * Words here actually mean any {@link String}, even ones that include
 * non-alphanumeric characters.
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public interface IPhraseConstructor {
    String construct(List<String> words);
}

