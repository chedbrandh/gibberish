package com.chedbrandh.gibberish;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Arrays;

import static com.chedbrandh.gibberish.PhraseTranslatorEndToEndTest.intsToBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Note that all bit sequences, between some start and end bit index, are
 * written and read as little-endian representations of some value.
 */
public class IndexTranslatorTest {

    @Test
    public void testFromBytes() throws Exception {
        IndexTranslator undertest = new IndexTranslator(ImmutableList.of(2, 1));

        // 10->0b|010|10000->0b010 => 0b01->2, 0b0->0
        assertEquals(ImmutableList.of(2, 0), undertest.fromBytes(intsToBytes(10), 0, 3));
        // 10->0b0|101|0000->0b101 => 0b10->1, 0b1->1
        assertEquals(ImmutableList.of(1, 1), undertest.fromBytes(intsToBytes(10), 1, 4));
        // 10->0b01|010|000->0b010 => 0b01->2, 0b0->0
        assertEquals(ImmutableList.of(2, 0), undertest.fromBytes(intsToBytes(10), 2, 5));
        // 10->0b010|100|00->0b100 => 0b10->1, 0b0->0
        assertEquals(ImmutableList.of(1, 0), undertest.fromBytes(intsToBytes(10), 3, 6));
    }

    @Test
    public void testToBytes() throws Exception {
        IndexTranslator undertest = new IndexTranslator(ImmutableList.of(2, 1));
        byte[] bytes;

        // 2->0b01, 0->0b0 => 0b010->0b|010|00000->2
        bytes = intsToBytes(0);
        undertest.toBytes(bytes, ImmutableList.of(2, 0), 0, 3);
        assertTrue(Arrays.equals(intsToBytes(2), bytes));

        // 2->0b01, 0->0b0 => 0b010->0b0|010|0000->4
        bytes = intsToBytes(0);
        undertest.toBytes(bytes, ImmutableList.of(2, 0), 1, 4);
        assertTrue(Arrays.equals(intsToBytes(4), bytes));

        // 3->0b11, 1->0b1 => 0b111->0b|111|00000->7
        bytes = intsToBytes(0);
        undertest.toBytes(bytes, ImmutableList.of(3, 1), 0, 3);
        assertTrue(Arrays.equals(intsToBytes(7), bytes));

        // 0->0b00, 1->0b1 => 0b001->0b|001|00000->4
        bytes = intsToBytes(0);
        undertest.toBytes(bytes, ImmutableList.of(0, 1), 0, 3);
        assertTrue(Arrays.equals(intsToBytes(4), bytes));

        // 2->0b01, 0->0b0 write to 8->0b00010000 =>
        // 0b010->0b|010|0000 write to 0b00010000 => 0b01010000->10
        bytes = intsToBytes(8);
        undertest.toBytes(bytes, ImmutableList.of(2, 0), 0, 3);
        assertTrue(Arrays.equals(intsToBytes(10), bytes));

        // 2->0b01, 0->0b0 write to 8->0b00010000 =>
        // 0b010->0b0|010|000 write to 0b00010000 => 0b00100000->4
        bytes = intsToBytes(8);
        undertest.toBytes(bytes, ImmutableList.of(2, 0), 1, 4);
        assertTrue(Arrays.equals(intsToBytes(4), bytes));

        // 2->0b01, 0->0b0 write to 64,3->0b0000001011000000 =>
        // 0b010->0b000000|010|0000000 write to 0b0000001011000000 => 0b0000000101000000->128,2
        bytes = intsToBytes(64, 3);
        undertest.toBytes(bytes, ImmutableList.of(2, 0), 6, 9);
        assertTrue(Arrays.equals(intsToBytes(128,2), bytes));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromBytesIllegalBitIndices() throws Exception {
        IndexTranslator undertest = new IndexTranslator(ImmutableList.of(2, 1));
        undertest.fromBytes(intsToBytes(1), 2, 7);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testFromBytesBitIndicesOutOfBounds() throws Exception {
        IndexTranslator undertest = new IndexTranslator(ImmutableList.of(2, 1));
        undertest.fromBytes(intsToBytes(1), 7, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBytesIllegalBitIndices() throws Exception {
        IndexTranslator undertest = new IndexTranslator(ImmutableList.of(2, 1));
        undertest.toBytes(intsToBytes(0), ImmutableList.of(2, 0), 2, 7);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testToBytesBitIndicesOutOfBounds() throws Exception {
        IndexTranslator undertest = new IndexTranslator(ImmutableList.of(2, 1));
        undertest.toBytes(intsToBytes(0), ImmutableList.of(2, 0), 7, 10);
    }
}
