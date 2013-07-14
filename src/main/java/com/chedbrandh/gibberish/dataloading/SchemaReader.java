package com.chedbrandh.gibberish.dataloading;

import com.chedbrandh.gibberish.IndexTranslator;
import com.chedbrandh.gibberish.PhraseTranslator;
import com.chedbrandh.gibberish.SeparatorsPhraseConstructor;
import com.chedbrandh.gibberish.WordProvider;
import com.chedbrandh.gibberish.WordProviderSequence;
import com.chedbrandh.gibberish.exceptions.BitCoverageException;
import com.chedbrandh.gibberish.exceptions.DuplicateWordException;
import com.chedbrandh.gibberish.optimization.ConstrainedIntegerOptimizer;
import com.chedbrandh.gibberish.optimization.TotalMeanWordLengthProblem;
import com.chedbrandh.gibberish.optimization.TotalMeanWordLengthProblem.Direction;
import com.chedbrandh.gibberish.optimization.TotalMeanWordLengthProblem.Input;
import com.chedbrandh.gibberish.optimization.TotalMeanWordLengthProblem.Output;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Class for reading schemas and creating translators.
 *
 * Files referenced from schema are first searched for in the classpath, then
 * if not found, it is searched for in the file system.
 *
 * If the schema specifies a checksum for a translator, then a match between
 * this expected checksum and the computed checksum for the translator is
 * verified.
 *
 * If no bit distribution for a translator is given, and optimization algorithm
 * is ran to find the bit distribution that creates the shortest phrases.
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public class SchemaReader {

    private static final String BAD_CHECKSUM_FORMAT =
            "Expected checksum %s does not match computed checksum %s for translator '%s'.";

    private final Schema schema;
    private final Map<String, WordFileReader> files = Maps.newHashMap();
    private final Map<String, WordProvider> providers = Maps.newHashMap();
    private final Map<String, PhraseTranslator> translators = Maps.newHashMap();

    /**
     * Create a SchemaReader from a Schema InputStream.
     *
     * @param inputStream               Schema InputStream to read.
     * @throws IOException              Thrown if problems reading word files.
     * @throws DuplicateWordException   Thrown if any word file contains duplicate words.
     * @throws BitCoverageException     Thrown if not enough words to cover bit coverage
     *                                  requirement.
     */
    public SchemaReader(InputStream inputStream)
            throws IOException, DuplicateWordException, BitCoverageException {
        this(new Yaml().loadAs(inputStream, Schema.class));
    }

    /**
     * Create a SchemaReader from a Schema.
     *
     * @param schema                    Schema specifying word files and how to construct
     *                                  phrase translators.
     * @throws IOException              Thrown if problems reading word files.
     * @throws DuplicateWordException   Thrown if any word file contains duplicate words.
     * @throws BitCoverageException     Thrown if not enough words to cover bit coverage
     *                                  requirement.
     */
    public SchemaReader(Schema schema)
            throws IOException, DuplicateWordException, BitCoverageException {
        schema.verifySemantics();
        this.schema = schema;
        loadFiles();
        loadProviders();
        loadTranslators();
    }

    /**
     * Returns a map with the translators described by the schema.
     *
     * @return Map from translator reference name to translator.
     */
    public ImmutableMap<String, PhraseTranslator> getTranslators() {
        return ImmutableMap.copyOf(translators);
    }

    /**
     * Load files specified by schema.
     *
     * @throws IOException              Thrown if problems reading word files.
     * @throws DuplicateWordException   Thrown if any word file contains duplicate words.
     */
    private void loadFiles() throws IOException, DuplicateWordException {
        // for each schema file entry
        for (Map.Entry<String, Schema.File> fileEntry : schema.files.entrySet()) {
            // read word file, create a reader, and add to map
            String path = fileEntry.getValue().path;
            WordFileReader wordFileReader = new WordFileReader
                    (new FileInputStream(path), fileEntry.getKey());
            files.put(fileEntry.getKey(), wordFileReader);
        }
    }

    /**
     * Create new word providers using the files specified by the schema.
     */
    private void loadProviders() {
        // for each schema provider entry
        for (Map.Entry<String, Schema.Provider> providerEntry : schema.providers.entrySet()) {
            // collect all words in all referenced files
            List<String> words = Lists.newArrayList();
            for(String wordFileReference : providerEntry.getValue().files) {
                words.addAll(files.get(wordFileReference).getWordSet());
            }
            // create provider and add to map
            WordProvider provider = new WordProvider(words, providerEntry.getKey());
            providers.put(providerEntry.getKey(), provider);
        }
    }

    /**
     * Create new phrase translators using the providers specified by the schema.
     *
     * If no bit distribution is specified by the schema, an optimizer is used
     * to determine the bit distribution that gives the shortest phrases.
     *
     * Also any provided checksum is verified.
     *
     * @throws BitCoverageException Thrown if not enough words to cover bit coverage requirement.
     */
    private void loadTranslators() throws BitCoverageException {
        // for each schema translator entry
        for (Map.Entry<String, Schema.Translator> translatorEntry :
                schema.translators.entrySet()) {
            String translatorReference = translatorEntry.getKey();
            Schema.Translator translatorSchema = translatorEntry.getValue();

            // create word providers
            Iterable<WordProvider> providers = translatorSchema.providers.stream()
                    .map(Functions.forMap(this.providers)::apply)
                    .collect(Collectors.toList());
            WordProviderSequence wordProviderSequence = new WordProviderSequence(providers);

            // create index translator
            List<Integer> bitDistribution = getBitDistribution(translatorSchema, providers);
            IndexTranslator indexTranslator = new IndexTranslator(bitDistribution);

            // create phrase constructor
            SeparatorsPhraseConstructor phraseConstructor =
                    new SeparatorsPhraseConstructor(translatorSchema.format);

            // verify any provided checksum
            if (translatorSchema.checksum != null) {
                verifyChecksum(translatorReference,
                               wordProviderSequence,
                               translatorSchema.checksum);
            }

            // create phrase translator
            PhraseTranslator phraseTranslator = new PhraseTranslator(
                    wordProviderSequence, indexTranslator, phraseConstructor, phraseConstructor);

            // add phrase translator to map
            translators.put(translatorReference, phraseTranslator);
        }
    }

    /**
     * If a bit distribution is provided then this is used, otherwise an
     * optimization algorithm is ran to produce the bit distribution that
     * creates the shortest phrases for a translator.
     *
     * @param translator    Translator schema.
     * @param providers     Word providers to use for creating bit distribution.
     * @return              The computed optimal bit distribution (or what schema specifies.)
     */
    private static List<Integer> getBitDistribution(
            Schema.Translator translator, Iterable<WordProvider> providers) {
        if (translator.bit_distribution != null) {
            return translator.bit_distribution;
        }
        TotalMeanWordLengthProblem problem =
                new TotalMeanWordLengthProblem(providers, translator.number_of_bits);
        ConstrainedIntegerOptimizer<Input, Output, Direction> optimizer =
                new ConstrainedIntegerOptimizer<>(problem);
        return optimizer.findMin().bitDistribution;
    }

    /**
     * Computes the checksum of a word provider sequence and throws an exception if the
     * expected checksum does not match the computed.
     *
     * @param translatorReference   Translator reference name used in Schema.
     * @param wordProviderSequence  WordProviderSequence to compute checksum for.
     * @param expectedChecksum      Checksum to expect from the word providers.
     */
    private static void verifyChecksum(String translatorReference,
                                       WordProviderSequence wordProviderSequence,
                                       String expectedChecksum) {
        String computedChecksum = wordProviderSequence.computeChecksum();
        if (!expectedChecksum.equals(computedChecksum)) {
            throw new IllegalArgumentException(String.format(
                    BAD_CHECKSUM_FORMAT, expectedChecksum, computedChecksum, translatorReference));
        }
    }
}
