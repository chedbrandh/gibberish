package com.chedbrandh.gibberish;

import com.google.common.base.Preconditions;

import java.util.BitSet;

/**
 * Class that facilitates reading and writing of longs to a {@link BitSet}. Longs
 * are read and written with the least significant bit at the first index.
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public class BitSetWithLongs extends BitSet {

    /**
     * Write long at specified index with the least significant bit first.
     *
     * Any bits after (toIndex - fromIndex) in the long will be ignored.
     *
     * @param fromIndex index of the first bit to be written.
     * @param toIndex index after the last bit to be written.
     * @param value the long value to write.
     */
    public void setLong(int fromIndex, int toIndex, long value) {
        Preconditions.checkArgument(value >= 0);
        Preconditions.checkArgument(fromIndex < toIndex);
        Preconditions.checkArgument(toIndex - fromIndex <= Long.SIZE);
        for (int i = 0; i < toIndex - fromIndex; i++) {
            set(fromIndex + i, ((value >> i) & 1) == 1);
        }
    }

    /**
     * Read long at specified index with the least significant bit first.
     *
     * @param fromIndex index of the first bit to be read.
     * @param toIndex index after the last bit to be read.
     * @return the read long value.
     */
    public long getLong(int fromIndex, int toIndex) {
        Preconditions.checkArgument(fromIndex < toIndex);
        Preconditions.checkArgument(toIndex - fromIndex <= Long.SIZE);
        long result = 0;
        for (int i = 0; i < toIndex - fromIndex; i++) {
            result += get(fromIndex + i) ? 1 << i : 0;
        }
        return  result;
    }

    /**
     * Returns a new bit set containing all the bits in the given byte array.
     *
     * <p>More precisely,
     * <br>{@code valueOf(bytes, from, to).get(n) == ((bytes[n/8] & (1<<(n%8))) != 0)}
     * <br>for all {@code from <= n < to}.
     *
     * @param bytes a byte array containing a little-endian
     *        representation of a sequence of bits to be used as the
     *        initial bits of the new bit set.
     * @param fromBitIndex index of the first bit to be copied.
     * @param toBitIndex index after the last bit to be copied.
     * @return the created BitSetWithLongs.
     */
    public static BitSetWithLongs valueOf(byte[] bytes, int fromBitIndex, int toBitIndex) {
        Preconditions.checkArgument(fromBitIndex <= toBitIndex);
        Preconditions.checkArgument(toBitIndex <= bytes.length * Byte.SIZE);
        BitSetWithLongs result = new BitSetWithLongs();
        for (int i = fromBitIndex; i < toBitIndex; i++) {
            int byteIndex = i / Byte.SIZE;
            int byteOffset = i % Byte.SIZE;
            int mask = 1 << byteOffset;
            result.set(i, (bytes[byteIndex] & mask) != 0);
        }
        return result;
    }

    /**
     * Populates a byte array with all the specified bits in this bit set.
     *
     * <p>More precisely, if
     * <br>{@code bitSet.toByteArray(bytes, from, to);}
     * <br>then {@code bitSet.get(n) == ((bytes[n/8] & (1<<(n%8))) != 0)}
     * <br>for all {@code from <= n < to}.
     *
     * @param bytes a byte array for writing a little-endian representation
     *              of the specified bits in this bit set.
     * @param fromBitIndex index of the first bit to be copied.
     * @param toBitIndex index after the last bit to be copied.
     */
    public void toByteArray(byte[] bytes, int fromBitIndex, int toBitIndex) {
        Preconditions.checkArgument(fromBitIndex <= toBitIndex);
        Preconditions.checkArgument(toBitIndex <= bytes.length * Byte.SIZE);
        for (int i = fromBitIndex; i < toBitIndex; i++) {
            int byteIndex = i / Byte.SIZE;
            int byteOffset = i % Byte.SIZE;
            int mask = 1 << byteOffset;
            int oldByte = bytes[byteIndex] & 0xFF;
            bytes[byteIndex] = (byte) (get(i) ? oldByte | mask : oldByte & ~mask);
        }
    }
}
