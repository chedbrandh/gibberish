package com.chedbrandh.gibberish;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.chedbrandh.gibberish.exceptions.IllegalPhraseException;

import java.util.List;


/**
 * Implementation of a phrase constructor/destructor inserts separators between
 * the words used to construct the phrase.
 *
 * For example separators ["", " like to eat ", "."] applied to the words
 * ["Dingos", "moussaka"] becomes "Dingos like to eat moussaka."
 *
 * The algorithm is implemented in such a way that it allows for both words and
 * separators to contain regex and {@link java.util.Formatter} characters. This
 * means that "words" don't technically have to be words since they are allowed
 * to contain non-alphanumeric characters.
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public class SeparatorsPhraseConstructor implements IPhraseConstructor, IPhraseDeconstructor {

    private final ImmutableList<String> separators;
    private final String leading;
    private final String trailing;

    /**
     * Creates a SeparatorsPhraseConstructor from some separators.
     *
     * The number of separators provided should be one more than the number of
     * words used to construct phrases. I.e. The first and the last Strings in
     * the separators list will not technically be used as separators, but
     * rather the first and last Strings will provide the "prefix" and "suffix"
     * of the phrase.
     *
     * All separators must be non-empty except for the first and the last
     * String in the separators list.
     *
     * @param separators    Iterable of Strings to be used for separating words.
     */
    public SeparatorsPhraseConstructor(List<String> separators) {
        Preconditions.checkArgument(separators.size() >= 2,
                "Must provide at least two separators.");

        this.leading = Strings.nullToEmpty(separators.get(0));
        this.trailing = Strings.nullToEmpty(separators.get(separators.size() - 1));
        this.separators = ImmutableList.copyOf(separators.subList(1, separators.size() - 1));

        for (String separator : this.separators) {
            Preconditions.checkArgument(!separator.isEmpty(), "Separators must not be empty.");
        }
    }

    /**
     * Constructs a phrase from words.
     *
     * Strings from the separators and the words are concatenated
     * alternatively to create a phrase.
     *
     * @param words   Parts used to construct a phrase. Typically words.
     * @return        Phrase constructed from words and separators.
     */
    @Override
    public String construct(List<String> words) {
        // note that in documentation terminology, separators include leading and trailing
        Preconditions.checkArgument(words.size() == separators.size() + 1,
                "Number of words must be one less than the number of separators.");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(leading);
        stringBuilder.append(words.get(0));
        for (int i = 0; i < separators.size(); i++) {
            stringBuilder.append(separators.get(i));
            stringBuilder.append(words.get(i + 1));
        }
        stringBuilder.append(trailing);

        return stringBuilder.toString();
    }

    /**
     * Deconstructs a phrase into words.
     *
     * Separators are used to divide up the phrase into words. The list
     * of words is then returned.
     *
     * @param phrase                    Phrase to deconstruct.
     * @return                          List of words.
     * @throws IllegalPhraseException   If separators are not found in the
     *                                  phrase, in the expected places.
     */
    @Override
    public List<String> deconstruct(String phrase) throws IllegalPhraseException {

        // strip any leading phrase "prefix" or "postfix"
        phrase = stripLeading(phrase);
        phrase = stripTrailing(phrase);

        int index = 0;
        // get all but last words
        ImmutableList.Builder<String> words = ImmutableList.builder();
        for (String separator : separators) {
            int separatorIndex = phrase.indexOf(separator, index);
            if (separatorIndex == -1) {
                throw IllegalPhraseException.expectedSeparator(phrase, separator);
            }
            words.add(phrase.substring(index, separatorIndex));
            index = separatorIndex + separator.length();
        }

        // get last word
        words.add(phrase.substring(index));

        return words.build();
    }

    /**
     * Strip leading substring from a phrase String.
     *
     * @param phrase                    Phrase to strip.
     * @return                          Phrase with leading substring removed.
     * @throws IllegalPhraseException   If leading substring can't be found.
     */
    private String stripLeading(String phrase) throws IllegalPhraseException {
        if (leading.length() > phrase.length() ||
                !leading.equals(phrase.substring(0, leading.length()))) {
            throw IllegalPhraseException.expectedLeading(phrase, leading);
        }
        return phrase.substring(leading.length());
    }

    /**
     * String trailing substring from a phrase String.
     *
     * @param phrase                    Phrase to strip.
     * @return                          Phrase with trailing substring removed.
     * @throws IllegalPhraseException   If trailing substring can't be found.
     */
    private String stripTrailing(String phrase) throws IllegalPhraseException {
        if (trailing.length() > phrase.length() ||
                !trailing.equals(phrase.substring(phrase.length() - trailing.length()))) {
            throw IllegalPhraseException.expectedTrailing(phrase, trailing);
        }
        return phrase.substring(0, phrase.length() - trailing.length());
    }
}
