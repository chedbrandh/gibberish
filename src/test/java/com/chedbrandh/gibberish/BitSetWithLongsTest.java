package com.chedbrandh.gibberish;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class BitSetWithLongsTest {

    @Test
    public void testSetLong() throws Exception {
        BitSetWithLongs bitSet = new BitSetWithLongs();
        bitSet.setLong(0, 10, 6);
        assertFalse(bitSet.get(0));
        assertTrue(bitSet.get(1));
        assertTrue(bitSet.get(2));
        assertFalse(bitSet.get(3));
        assertFalse(bitSet.get(4));
        assertFalse(bitSet.get(5));
    }

    @Test
    public void testSetLongCutOff() throws Exception {
        BitSetWithLongs bitSet = new BitSetWithLongs();
        bitSet.setLong(1, 3, 15);
        assertFalse(bitSet.get(0));
        assertTrue(bitSet.get(1));
        assertTrue(bitSet.get(2));
        assertFalse(bitSet.get(3));
        assertFalse(bitSet.get(4));
        assertFalse(bitSet.get(5));
    }

    @Test
    public void testGetLong() throws Exception {
        BitSetWithLongs bitSet = new BitSetWithLongs();
        bitSet.set(1, true);
        bitSet.set(3, true);
        assertEquals(10, bitSet.getLong(0, 10));
    }

    @Test
    public void testGetLongCutOff() throws Exception {
        BitSetWithLongs bitSet = new BitSetWithLongs();
        bitSet.setLong(0, 10, 14);
        assertEquals(7, bitSet.getLong(1, 10));
        assertEquals(6, bitSet.getLong(0, 3));
    }

    @Test
    public void testValueOfByteArray() throws Exception {
        byte[] bytes = new byte[] {1, (byte)0xFF};
        assertTrue(BitSetWithLongs.valueOf(bytes, 0, 16).get(0));
        assertFalse(BitSetWithLongs.valueOf(bytes, 0, 16).get(1));
        assertFalse(BitSetWithLongs.valueOf(bytes, 0, 16).get(7));
        assertTrue(BitSetWithLongs.valueOf(bytes, 0, 16).get(8));
        assertTrue(BitSetWithLongs.valueOf(bytes, 0, 16).get(9));
        assertTrue(BitSetWithLongs.valueOf(bytes, 0, 16).get(15));
        assertFalse(BitSetWithLongs.valueOf(bytes, 1, 16).get(0));
        assertFalse(BitSetWithLongs.valueOf(bytes, 0, 8).get(8));
        assertTrue(BitSetWithLongs.valueOf(bytes, 8, 9).get(8));
        assertFalse(BitSetWithLongs.valueOf(bytes, 9, 15).get(8));
        assertFalse(BitSetWithLongs.valueOf(bytes, 0, 15).get(15));
    }

    @Test
    public void testToByteArraySomeOnes() throws Exception {
        byte[] bytes = new byte[1];
        BitSetWithLongs bitSet = new BitSetWithLongs();
        bitSet.setLong(0, 8, 0xFF);
        bitSet.toByteArray(bytes, 2, 6);
        assertEquals(60, bytes[0]);
    }

    @Test
    public void testToByteArraySomeZeros() throws Exception {
        byte[] bytes = new byte[] {(byte) 0xFF};
        BitSetWithLongs bitSet = new BitSetWithLongs();
        bitSet.toByteArray(bytes, 1, 7);
        assertEquals(129, bytes[0] & 0xFF);
    }
}
