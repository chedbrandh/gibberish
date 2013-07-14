package com.chedbrandh.gibberish.dataloading;

import com.google.common.collect.Iterables;

import java.util.List;
import java.util.Map;

/**
 * Class for defining translators.
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public class Schema {

    // semantics verification failure messages
    private static final String MISSING_REQUIRED_KEY_FORMAT =
            "Required key '%s' could not be found.";
    private static final String EMPTY_ITERABLE_FORMAT =
            "Value for key '%s' must be non-empty.";
    private static final String REFERENCE_NOT_FOUND_FORMAT =
            "Reference to %s '%s' could not be found.";
    private static final String FORMAT_PROVIDERS_LIST_SIZE_MESSAGE =
            "Size of list for key 'format' must be exactly one greater than 'providers'.";
    private static final String PROVIDERS_BIT_DISTRIBUTION_LIST_SIZE_MESSAGE =
            "Size of list for key 'providers' must be exactly the same as 'bit_distribution'.";
    private static final String BIT_DISTRIBUTION_SUM_MESSAGE =
            "Sum of integers in list for key 'bit_distribution' must equal 'number_of_bits'.";

    // map from file reference name to file
    public Map<String, File> files;

    // map from provider reference name to provider
    public Map<String, Provider> providers;

    // map from translator reference name to translator
    public Map<String, Translator> translators;

    /**
     * A File must specify the path to where a word file can be found.
     */
    public static class File {
        // location of file.
        public String path;
    }

    /**
     * A Provider is a list of files. This allows for combining multiple files
     * to create one word provider.
     */
    public static class Provider {
        // list of file reference names.
        public List<String> files;
    }

    /**
     * A Translator contains word providers and everything else needed to
     * create phrase translators.
     *
     * Providing a bit distribution is optional. If not provided bit
     * distribution will be calculated.
     *
     * If a checksum is specified, an exception is thrown if it does not
     * match the checksum computed from the word providers.
     */
    public static class Translator {
        // list of provider reference names to use for phrase translation.
        public List<String> providers;
        // format to use for transforming a list of words into a phrase.
        public List<String> format;
        // number of bits used for phrase generation.
        public int number_of_bits;
        // optional assignment of number of bits per word provider.
        public List<Integer> bit_distribution;
        // optional checksum specified to ensure no changes made to word providers.
        public String checksum;
    }

    /**
     * Throws a runtime exception if semantics are illegal.
     *
     * Verifies that required keys are present, and that lists and maps are
     * non-empty.
     *
     * Verifies that file references made by providers, and provider references
     * made by translators, exist.
     *
     * Verifies that the size of the list for the 'format' key is exactly one
     * greater than that of the 'providers' key, for all translators.
     *
     * Verifies that the size of the list for the 'providers' key is exactly
     * the same as that of the 'bit_distribution' key, for all translators,
     * if the 'bit_distribution' key is set.
     *
     * Verifies the sum of all integers in the list for the 'bit_distribution'
     * key equals to the value of the 'number_of_bits' key, for all
     * translators, if both keys are available.
     */
    public void verifySemantics() {
        verifyNotNullOrEmpty("files", files);
        verifyNotNullOrEmpty("providers", providers);
        verifyNotNullOrEmpty("translators", translators);
        files.values().forEach(Schema::verifyFileSemantics);
        providers.values().forEach(this::verifyProviderSemantics);
        translators.values().forEach(this::verifyTranslatorSemantics);
    }

    private static void verifyFileSemantics(File file) {
        verifyNotNull("path", file.path);
    }

    private void verifyProviderSemantics(Provider provider) {
        verifyNotNullOrEmpty("files", provider.files);
        // verify file references' existence
        for (String fileKey : provider.files) {
            if (!files.containsKey(fileKey)) {
                throw new IllegalArgumentException(
                        String.format(REFERENCE_NOT_FOUND_FORMAT, "file", fileKey));
            }
        }
    }

    private void verifyTranslatorSemantics(Translator translator) {
        verifyNotNullOrEmpty("format", translator.format);
        verifyNotNullOrEmpty("providers", translator.providers);
        if (translator.number_of_bits == 0) {
            verifyNotNullOrEmpty("bit_distribution", translator.bit_distribution);
        }

        // verify provider references' existence
        for (String providerKey : translator.providers) {
            if (!providers.containsKey(providerKey)) {
                throw new IllegalArgumentException(
                        String.format(REFERENCE_NOT_FOUND_FORMAT, "provider", providerKey));
            }
        }

        // verify format size == providers size + 1
        if (translator.format.size() != translator.providers.size() + 1) {
            throw new IllegalArgumentException(FORMAT_PROVIDERS_LIST_SIZE_MESSAGE);
        }
        // verify providers size == bit_distribution size, if available
        if (translator.bit_distribution != null &&
                translator.providers.size() != translator.bit_distribution.size()) {
            throw new IllegalArgumentException(PROVIDERS_BIT_DISTRIBUTION_LIST_SIZE_MESSAGE);
        }
        // verify sum bit_distribution == number_of_bits, if available
        if (translator.bit_distribution != null && translator.number_of_bits > 0 &&
                sum(translator.bit_distribution) != translator.number_of_bits) {
            throw new IllegalArgumentException(BIT_DISTRIBUTION_SUM_MESSAGE);
        }
    }

    private static void verifyNotNull(String key, Object value) {
        if (value == null) {
            throw new IllegalArgumentException(String.format(MISSING_REQUIRED_KEY_FORMAT, key));
        }
    }

    private static void verifyNotNullOrEmpty(String key, Iterable value) {
        verifyNotNull(key, value);
        if (Iterables.isEmpty(value)) {
            throw new IllegalArgumentException(String.format(EMPTY_ITERABLE_FORMAT, key));
        }
    }

    private static void verifyNotNullOrEmpty(String key, Map value) {
        verifyNotNull(key, value);
        if (value.isEmpty()) {
            throw new IllegalArgumentException(String.format(EMPTY_ITERABLE_FORMAT, key));
        }
    }

    private static int sum(List<Integer> integerList) {
        int result = 0;
        for (int i : integerList) {
            result += i;
        }
        return result;
    }
}
