package com.chedbrandh.gibberish;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;

import java.util.List;


/**
 * Translates between bit sequences and lists of indices.
 *
 * A bit sequence is divided up in to smaller bit subsequences according to a
 * bit distribution. The bit subsequences are then interpreted as
 * integers/indices.
 *
 * The bit distribution is a list of the number of bits that each index is
 * assigned. For example a bit distribution [2, 1, 3] would divide a bit
 * sequence 0b011011 into the bit subsequences [0b01, 0b1, 0b011]. These bit
 * subsequences then are interpreted as little-endian integers [2, 1, 6].
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public class IndexTranslator {

    private static final String ILLEGAL_BIT_INDICES_FORMAT =
            "The number of bits covered by the specified bit range must equal %s.";
    private static final String BIT_INDEX_OUT_OF_BOUNDS =
            "Bit indices must be in the range of the specified byte array.";

    // bit distribution determining how to split bit sequences
    private final ImmutableList<Integer> bitDistribution;

    // number of bits that the bit distribution covers
    private final int bitCoverage;

    /**
     * Creates an IndexTranslator for a given bit distribution.
     *
     * @param bitDistribution   Bit distribution to use for translating
     *                          indices. Bit sequences are split into bit
     *                          subsequences determined by this distribution.
     */
    public IndexTranslator(Iterable<Integer> bitDistribution) {
        this.bitDistribution = ImmutableList.copyOf(bitDistribution);

        // set bit coverage
        int bitSum = 0;
        for (int bits : bitDistribution) {
            bitSum += bits;
        }
        bitCoverage = bitSum;
    }

    /**
     * Reads bits from a byte array interpreting them as little-endian
     * integers.
     *
     * The number of bits to read must equal the sum of all bits in the bit
     * distribution.
     *
     * @param bytes         Byte array to read bits from.
     * @param fromBitIndex  Index of first bit to read, inclusive.
     * @param toBitIndex    Index of last bit to read, exclusive.
     * @return              List of little-endian integers read from the given bytes.
     */
    public List<Integer> fromBytes(byte[] bytes, int fromBitIndex, int toBitIndex) {
        verifyBitIndices(bytes, fromBitIndex, toBitIndex);
        BitSetWithLongs bitSet = BitSetWithLongs.valueOf(bytes, fromBitIndex, toBitIndex);
        ImmutableList.Builder<Integer> builder = ImmutableList.builder();
        int bitIndex = fromBitIndex;
        for (int numBits : bitDistribution) {
            builder.add(Ints.checkedCast(bitSet.getLong(bitIndex, bitIndex + numBits)));
            bitIndex += numBits;
        }
        return builder.build();
    }

    /**
     * Writes bits to a byte array interpreting them as little-endian integers.
     *
     * @param bytes         Byte array to write bits to.
     * @param indices       List of little-endian integers write to the given bytes.
     * @param fromBitIndex  Index of first bit to write, inclusive.
     * @param toBitIndex    Index of last bit to write, exclusive.
     */
    public void toBytes(byte[] bytes, List<Integer> indices, int fromBitIndex, int toBitIndex) {
        verifyBitIndices(bytes, fromBitIndex, toBitIndex);
        BitSetWithLongs bitSet = new BitSetWithLongs();
        int subFromBitIndex = fromBitIndex;
        for (int i = 0; i < indices.size(); i++) {
            int numBits = bitDistribution.get(i);
            int index = indices.get(i);
            bitSet.setLong(subFromBitIndex, subFromBitIndex + numBits, index);
            subFromBitIndex += numBits;
        }
        bitSet.toByteArray(bytes, fromBitIndex, toBitIndex);
    }

    /**
     * Get the number of bits that the bit distribution covers.
     *
     * @return  The sum of all integers in the bit distribution.
     */
    public int bitCoverage() {
        return bitCoverage;
    }

    /**
     * Get the bit distribution used by this IndexTranslator.
     *
     * @return  Bit distribution used for translating indices.
     */
    public List<Integer> bitDistribution() {
        return bitDistribution;
    }

    /**
     * Throws runtime exception if bit indices are bad.
     */
    private void verifyBitIndices(byte[] bytes, int fromBitIndex, int toBitIndex) {
        if (!(0 <= fromBitIndex && toBitIndex <= bytes.length * Byte.SIZE)) {
            throw new IndexOutOfBoundsException(BIT_INDEX_OUT_OF_BOUNDS);
        }
        if (toBitIndex - fromBitIndex != bitCoverage()) {
            throw new IllegalArgumentException(
                    String.format(ILLEGAL_BIT_INDICES_FORMAT, bitCoverage()));
        }
    }
}
