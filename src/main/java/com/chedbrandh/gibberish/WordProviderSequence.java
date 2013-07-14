package com.chedbrandh.gibberish;

import com.chedbrandh.gibberish.exceptions.IllegalWordException;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.chedbrandh.gibberish.exceptions.BitCoverageException;
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Class containing a list of WordProviders.
 *
 * By providing indices words can be gotten from those WordProviders, and
 * a list of words can be translated into a list of indices.
 *
 * The functionality of calculating bit coverage and checksums is also
 * available.
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public class WordProviderSequence {

    private final ImmutableList<WordProvider> wordProviders;

    /**
     * Constructs a WordProviderSequence from some word providers.
     * @param wordProviders     The word providers that makes the sequence.
     */
    public WordProviderSequence(Iterable<WordProvider> wordProviders) {
        this.wordProviders = ImmutableList.copyOf(wordProviders);
    }

    /**
     * Get one word from each word provider according to some indices.
     *
     * @param indices   The indices to use for looking up words in the word
     *                  providers. The number of indices must match the number
     *                  of word providers.
     * @return          The list of requested words.
     */
    public List<String> getWords(List<Integer> indices) {
        Preconditions.checkArgument(indices.size() == wordProviders.size(),
                "Number of words requested does not match the number of word providers.");
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (int i = 0; i < wordProviders.size(); i++) {
            builder.add(wordProviders.get(i).get(indices.get(i)));
        }
        return builder.build();
    }

    /**
     * Get the index for some word from each word provider.
     *
     * @param words                     The words to find indices in the word providers for. The
     *                                  number of words must match the number of word providers.
     * @throws IllegalWordException     Thrown if a word is not found in its WordProvider.
     * @return                          The list of requested indices.
     */
    public List<Integer> getIndices(List<String> words) throws IllegalWordException {
        Preconditions.checkArgument(words.size() == wordProviders.size(),
                "Number of indices requested does not match the number of word providers.");
        ImmutableList.Builder<Integer> builder = ImmutableList.builder();
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            WordProvider wordProvider = wordProviders.get(i);
            int index = wordProvider.indexOf(word);
            if (index == -1) {
                throw new IllegalWordException(word, wordProvider);
            }
            builder.add(index);
        }
        return builder.build();
    }

    /**
     * Returns the sum of the WordProviders bit coverage.
     *
     * @return The total bit coverage.
     */
    public int bitCoverage() {
        int sum = 0;
        for (WordProvider wordProvider : wordProviders) {
            sum += wordProvider.bitCoverage();
        }
        return sum;
    }

    /**
     * Verify the bit coverage for number for each word provider.
     *
     * @param bitDistribution       List of integers indicating the number of bits that
     *                              will be used for each word provider for phrase
     *                              translation. The number of bit assignments must
     *                              match the number of word providers.
     * @throws BitCoverageException Thrown if there aren't enough words in the word provider to
     *                              cover the number of bits assigned to it.
     */
    public void verifyProviderBitCoverage(List<Integer> bitDistribution)
            throws BitCoverageException {
        Preconditions.checkArgument(bitDistribution.size() == wordProviders.size(),
                "Number of bit distributions does not match the number of word providers.");
        for (int i = 0; i < wordProviders.size(); i++) {
            WordProvider provider = wordProviders.get(i);
            Integer numBits = bitDistribution.get(i);
            if (numBits > provider.bitCoverage()) {
                throw new BitCoverageException(provider, numBits);
            }
        }
    }

    /**
     * Compute and return the SHA-1 checksum of all ordered words in all
     * ordered word providers.
     *
     * @return The computed checksum.
     */
    public String computeChecksum() {
        MessageDigest messageDigest = createSha1MessageDigest();
        for (WordProvider provider : wordProviders) {
            for (String word : provider) {
                messageDigest.update(word.getBytes());
            }
        }
        return Hex.encodeHexString(messageDigest.digest());
    }

    /**
     * Create a SHA-1 message digest.
     */
    private static MessageDigest createSha1MessageDigest() {
        try {
            return MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw Throwables.propagate(e);
        }
    }
}
