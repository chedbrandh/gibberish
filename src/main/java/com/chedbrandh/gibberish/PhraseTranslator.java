package com.chedbrandh.gibberish;

import com.chedbrandh.gibberish.exceptions.BitCoverageException;
import com.chedbrandh.gibberish.exceptions.IllegalPhraseException;
import com.chedbrandh.gibberish.exceptions.IllegalWordException;
import com.chedbrandh.gibberish.exceptions.WordIndexOutOfBoundsException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import java.util.List;

/**
 * Class for translating between bit sequences and phrases.
 *
 * Subsequences of the bits are translated into an integer. This integer is
 * used as an index to find the corresponding word in a word list.
 *
 * The provided bits are chopped up into subsequences by the {@link IndexTranslator}
 * and interpreted as indices. The {@link WordProviderSequence} then takes these
 * indices to lookup with words the indices refer to. Finally the
 * {@link IPhraseConstructor} turns the sequence of words into a phrase. Translating a
 * phrase into a sequence of bits is the reverse of this process.
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public class PhraseTranslator {

    private static final String ILLEGAL_LONG_USAGE =
            "Longs can not provide the number of bits required.";

    private final int numBits;
    private final WordProviderSequence wordProviderSequence;
    private final IndexTranslator indexTranslator;
    private final IPhraseConstructor phraseConstructor;
    private final IPhraseDeconstructor phraseDeconstructor;

    /**
     * Create a translator. The {@link WordProviderSequence} must have enough
     * coverage to handle the indices that the IndexTranslator may provide.
     *
     * @param wordProviderSequence  Translates between indices and words.
     * @param indexTranslator       Translates between bit sequences and indices.
     * @param phraseConstructor     Translates word sequences to phrases.
     * @param phraseDeconstructor   Translates phrases to word sequences.
     * @throws BitCoverageException Thrown if there aren't enough words in a word provider to
     *                              cover the number of bits assigned to it.
     */
    public PhraseTranslator(WordProviderSequence wordProviderSequence,
                            IndexTranslator indexTranslator,
                            IPhraseConstructor phraseConstructor,
                            IPhraseDeconstructor phraseDeconstructor)
            throws BitCoverageException {
        this.wordProviderSequence = wordProviderSequence;
        this.indexTranslator = indexTranslator;
        this.phraseConstructor = phraseConstructor;
        this.phraseDeconstructor = phraseDeconstructor;
        this.numBits = indexTranslator.bitCoverage();

        // verify bit coverage
        wordProviderSequence.verifyProviderBitCoverage(
                indexTranslator.bitDistribution());
    }

    /**
     * Translates a sequence of bits in a byte array to a phrase.
     *
     * @param bytes         The byte array to translate from.
     * @param fromBitIndex  The inclusive bit start index to read from the byte array.
     * @param toBitIndex    The exclusive bit end index to read from the byte array.
     * @return              Returns the translated phrase as defined by the word providers.
     */
    public String fromBytes(byte[] bytes, int fromBitIndex, int toBitIndex) {
        List<Integer> indices = indexTranslator.fromBytes(bytes, fromBitIndex, toBitIndex);
        List<String> words = wordProviderSequence.getWords(indices);
        return phraseConstructor.construct(words);
    }

    /**
     * Translates a phrase to a sequence of bits written to a byte array.
     *
     * @param bytes                     The byte array to translate to.
     * @param phrase                    The phrase to translate from.
     * @param fromBitIndex              The inclusive bit start index to write to the byte array.
     * @param toBitIndex                The exclusive bit end index to write to the byte array.
     * @throws IllegalPhraseException   If phrase can't be translated.
     * @throws IllegalWordException     If a word can't be found in its word provider.
     * @throws WordIndexOutOfBoundsException    If word maps to illegal index.
     */
    public void toBytes(byte[] bytes, String phrase, int fromBitIndex, int toBitIndex)
            throws IllegalPhraseException, IllegalWordException, WordIndexOutOfBoundsException {
        // deconstruct phrase into words
        List<String> words = phraseDeconstructor.deconstruct(phrase);
        // map words to indices
        List<Integer> indices = wordProviderSequence.getIndices(words);
        // verify index legality
        WordIndexOutOfBoundsException.verifyIndexLegality(
                indices, words, indexTranslator.bitDistribution());
        // translate indices to bytes
        indexTranslator.toBytes(bytes, indices, fromBitIndex, toBitIndex);
    }

    /**
     * Translates the sequence of bits, in a long, to a phrase.
     *
     * @param l     Long to translate into a phrase.
     * @return      Translated phrase.
     */
    public String fromLong(long l) {
        byte[] bytes = longToBytes(l, numBits);
        return fromBytes(bytes, 0, numBits);
    }

    /**
     * Translates a phrase to a sequence of bits interpreted as a long.
     *
     * @param phrase                    Phrase to translate into a long.
     * @return                          Translated long.
     * @throws IllegalPhraseException   If phrase can't be translated.
     * @throws IllegalWordException     If a word can't be found in its word provider.
     * @throws WordIndexOutOfBoundsException    If word maps to illegal index.
     */
    public long toLong(String phrase)
            throws IllegalPhraseException, IllegalWordException, WordIndexOutOfBoundsException {
        byte[] bytes = new byte[numBitsToNumBytes(numBits)];
        toBytes(bytes, phrase, 0, numBits);
        return bytesToLong(bytes, numBits);
    }

    /**
     * Get {@link IndexTranslator} used to create the phrase translator.
     *
     * @return  The index translator.
     */
    public IndexTranslator getIndexTranslator() {
        return indexTranslator;
    }

    /**
     * Get {@link WordProviderSequence} used to create the phrase translator.
     *
     * @return  The word provider sequence.
     */
    public WordProviderSequence getWordProviderSequence() {
        return wordProviderSequence;
    }

    /**
     * Translates a long to a byte array.
     *
     * @param l         Long to translate.
     * @param numBits   Number of bits to set. (Sometimes not all bits in the
     *                  long are wanted)
     * @return          Byte array with bits from the long.
     */
    @VisibleForTesting
    protected static byte[] longToBytes(long l, int numBits) {
        Preconditions.checkState(numBits <= Long.SIZE, ILLEGAL_LONG_USAGE);
        Preconditions.checkArgument(0 <= l, "Only positive values are allowed.");
        BitSetWithLongs bitSet = new BitSetWithLongs();
        bitSet.setLong(0, numBits, l);
        byte[] bytes = new byte[numBitsToNumBytes(numBits)];
        bitSet.toByteArray(bytes, 0, numBits);
        return bytes;
    }

    /**
     * Translates a byte array to a long.
     *
     * @param bytes     Byte array with bits to turn into a long.
     * @param numBits   Number of bits to read from the byte array.
     * @return          Long read from the byte array.
     */
    @VisibleForTesting
    protected static long bytesToLong(byte[] bytes, int numBits) {
        Preconditions.checkState(numBits <= Long.SIZE, ILLEGAL_LONG_USAGE);
        BitSetWithLongs bitSet = BitSetWithLongs.valueOf(bytes, 0, numBits);
        return bitSet.getLong(0, numBits);
    }

    /**
     * Returns the number of bytes needed to hold some number of bits.
     *
     * @param numBits   The number of bits to be represented as bytes.
     * @return          The number of bytes needed.
     */
    @VisibleForTesting
    protected static int numBitsToNumBytes(int numBits) {
        Preconditions.checkArgument(numBits >= 0, "Number of bits less than zero.");
        return numBits / Byte.SIZE + (numBits % Byte.SIZE > 0 ? 1 : 0);
    }
}
