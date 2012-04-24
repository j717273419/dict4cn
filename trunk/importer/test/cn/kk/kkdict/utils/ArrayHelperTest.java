package cn.kk.kkdict.utils;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

public class ArrayHelperTest {
    @Test
    public void testFindTrimmedOffset() {
        byte[] test1 = { 1 };
        byte[] test2 = { '\t', 1, 2 };
        byte[] test3 = { Helper.SEP_NEWLINE_CHAR, '\0', '\0', 3, 4, 5, 6, '\0' };
        byte[] test4 = { '\0', Helper.SEP_NEWLINE_CHAR, ' ', '\t', '\0', 4, 3, 2, '\0', '\0', '\0' };
        byte[] test5 = { ' ', '\0', '\r', '\0', '\r', '\0', '\0' };
        byte[] test6 = {};

        ByteBuffer bb1 = ByteBuffer.wrap(test1);
        ByteBuffer bb2 = ByteBuffer.wrap(test2);
        ByteBuffer bb3 = ByteBuffer.wrap(test3);
        ByteBuffer bb4 = ByteBuffer.wrap(test4);
        ByteBuffer bb5 = ByteBuffer.wrap(test5);
        ByteBuffer bb6 = ByteBuffer.wrap(test6);

        assertEquals(0, ArrayHelper.findTrimmedOffset(bb1));
        assertEquals(0, ArrayHelper.findTrimmedOffsetP(bb1));
        assertEquals(1, ArrayHelper.findTrimmedOffset(bb2));
        assertEquals(1, ArrayHelper.findTrimmedOffsetP(bb2));
        assertEquals(3, ArrayHelper.findTrimmedOffset(bb3));
        assertEquals(3, ArrayHelper.findTrimmedOffsetP(bb3));
        assertEquals(5, ArrayHelper.findTrimmedOffset(bb4));
        assertEquals(5, ArrayHelper.findTrimmedOffsetP(bb4));
        assertEquals(bb5.limit(), ArrayHelper.findTrimmedOffset(bb5));
        assertEquals(bb5.limit(), ArrayHelper.findTrimmedOffsetP(bb5));
        assertEquals(0, ArrayHelper.findTrimmedOffset(bb6));
        assertEquals(0, ArrayHelper.findTrimmedOffsetP(bb6));

        bb3.position(2);
        assertEquals(3, ArrayHelper.findTrimmedOffset(bb3));
        assertEquals(3, ArrayHelper.findTrimmedOffsetP(bb3));
        bb3.position(4);
        assertEquals(3, ArrayHelper.findTrimmedOffset(bb3));
        assertEquals(4, ArrayHelper.findTrimmedOffsetP(bb3));
        bb2.position(2);
        assertEquals(1, ArrayHelper.findTrimmedOffset(bb2));
        assertEquals(2, ArrayHelper.findTrimmedOffsetP(bb2));
        bb4.position(10);
        assertEquals(5, ArrayHelper.findTrimmedOffset(bb4));
        assertEquals(bb4.limit(), ArrayHelper.findTrimmedOffsetP(bb4));
    }

    @Test
    public void testCompareTo() {
        byte[] test1 = { 1 };
        byte[] test2 = { 1, 2 };
        byte[] test3 = { 3, 4, 5, 6 };
        byte[] test4 = { 4, 3, 2 };
        byte[] test5 = { 4, 3, 2, 1, 0 };
        byte[] test6 = {};

        ByteBuffer bb1 = ByteBuffer.wrap(test5);
        ByteBuffer bb2 = ByteBuffer.wrap(test5);
        assertEquals(0, ArrayHelper.compareTo(bb1, bb2));
        assertEquals(0, ArrayHelper.compareToP(bb1, bb2));
        bb1 = ByteBuffer.wrap(test6);
        bb2 = ByteBuffer.wrap(test6);
        assertEquals(0, ArrayHelper.compareTo(bb1, bb2));
        assertEquals(0, ArrayHelper.compareToP(bb1, bb2));
        bb2 = ByteBuffer.wrap(test3);
        assertTrue(ArrayHelper.compareTo(bb1, bb2) < 0);
        assertTrue(ArrayHelper.compareToP(bb1, bb2) < 0);
        bb1 = ByteBuffer.wrap(test1);
        assertTrue(ArrayHelper.compareTo(bb1, bb2) < 0);
        assertTrue(ArrayHelper.compareToP(bb1, bb2) < 0);
        bb1 = ByteBuffer.wrap(test4);
        assertTrue(ArrayHelper.compareTo(bb1, bb2) > 0);
        assertTrue(ArrayHelper.compareToP(bb1, bb2) > 0);
        bb2 = ByteBuffer.wrap(test5);
        assertTrue(ArrayHelper.compareTo(bb1, bb2) < 0);
        assertTrue(ArrayHelper.compareToP(bb1, bb2) < 0);
        bb1 = ByteBuffer.wrap(test2);
        bb2 = ByteBuffer.wrap(test1);
        assertTrue(ArrayHelper.compareTo(bb1, bb2) > 0);
        assertTrue(ArrayHelper.compareToP(bb1, bb2) > 0);
        bb1 = ByteBuffer.wrap(test5);
        bb2 = ByteBuffer.wrap(test4);
        bb1.limit(3);
        assertTrue(ArrayHelper.compareTo(bb1, bb2) == 0);
        assertTrue(ArrayHelper.compareToP(bb1, bb2) == 0);
        bb1.position(1);
        assertTrue(ArrayHelper.compareTo(bb1, bb2) == 0);
        assertTrue(ArrayHelper.compareToP(bb1, bb2) < 0);
        bb2.position(1);
        assertTrue(ArrayHelper.compareTo(bb1, bb2) == 0);
        assertTrue(ArrayHelper.compareToP(bb1, bb2) == 0);
    }

    @Test
    public void testContains() {
        byte[] test0 = { 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6, 6, 5, 4, 3, 2, 1 };
        byte[] test1 = { 1 };
        byte[] test2 = { 1, 2 };
        byte[] test3 = { 3, 4, 5, 6 };
        byte[] test4 = { 4, 3, 2 };
        byte[] test5 = { 4, 3, 2, 1, 0 };
        byte[] test6 = {};

        ByteBuffer bb0 = ByteBuffer.wrap(test0);
        ByteBuffer bb1 = ByteBuffer.wrap(test1);
        ByteBuffer bb2 = ByteBuffer.wrap(test2);
        ByteBuffer bb3 = ByteBuffer.wrap(test3);
        ByteBuffer bb4 = ByteBuffer.wrap(test4);
        ByteBuffer bb5 = ByteBuffer.wrap(test5);
        ByteBuffer bb6 = ByteBuffer.wrap(test6);
        assertTrue(ArrayHelper.contains(bb0, bb3));
        assertTrue(ArrayHelper.containsP(bb0, bb3));
        assertTrue(ArrayHelper.contains(bb0, bb6));
        assertTrue(ArrayHelper.containsP(bb0, bb6));
        assertTrue(ArrayHelper.contains(bb5, bb5));
        assertTrue(ArrayHelper.containsP(bb5, bb5));
        assertTrue(ArrayHelper.contains(bb5, bb4));
        assertTrue(ArrayHelper.containsP(bb5, bb4));
        assertTrue(ArrayHelper.contains(bb6, bb6));
        assertTrue(ArrayHelper.containsP(bb6, bb6));
        assertFalse(ArrayHelper.contains(bb6, bb4));
        assertFalse(ArrayHelper.containsP(bb6, bb4));
        assertFalse(ArrayHelper.contains(bb1, bb2));
        assertFalse(ArrayHelper.containsP(bb1, bb2));
        assertFalse(ArrayHelper.contains(bb0, bb5));
        assertFalse(ArrayHelper.containsP(bb0, bb5));
        bb0.position(15);
        assertFalse(ArrayHelper.contains(bb5, bb0));
        assertTrue(ArrayHelper.containsP(bb5, bb0));
        bb0.limit(16);
        assertFalse(ArrayHelper.contains(bb3, bb0));
        assertTrue(ArrayHelper.containsP(bb3, bb0));
        bb3.position(1);
        assertFalse(ArrayHelper.contains(bb3, bb0));
        assertFalse(ArrayHelper.containsP(bb3, bb0));
    }

    @Test
    public void testCount() {
        byte[] data = { 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6, 6, 5, 4, 3, 2, 1 };
        byte[] test1 = { 1 };
        byte[] test2 = { 1, 2 };
        byte[] test3 = { 3, 4, 5, 6 };
        byte[] test4 = { 4, 3, 2 };
        byte[] test5 = { 4, 3, 2, 1, 0 };
        byte[] test6 = {};
        ByteBuffer bb = ByteBuffer.wrap(data);
        assertEquals(3, ArrayHelper.countP(bb, test1));
        assertEquals(2, ArrayHelper.countP(bb, test2));
        assertEquals(2, ArrayHelper.countP(bb, test3));
        assertEquals(1, ArrayHelper.countP(bb, test4));
        assertEquals(0, ArrayHelper.countP(bb, test5));
        assertEquals(0, ArrayHelper.countP(bb, test6));
        assertEquals(3, ArrayHelper.count(bb, test1));
        assertEquals(2, ArrayHelper.count(bb, test2));
        assertEquals(2, ArrayHelper.count(bb, test3));
        assertEquals(1, ArrayHelper.count(bb, test4));
        assertEquals(0, ArrayHelper.count(bb, test5));
        assertEquals(0, ArrayHelper.count(bb, test6));
        ByteBuffer bb1 = ByteBuffer.wrap(test1);
        ByteBuffer bb2 = ByteBuffer.wrap(test2);
        ByteBuffer bb3 = ByteBuffer.wrap(test3);
        ByteBuffer bb4 = ByteBuffer.wrap(test4);
        ByteBuffer bb5 = ByteBuffer.wrap(test5);
        ByteBuffer bb6 = ByteBuffer.wrap(test6);
        assertEquals(3, ArrayHelper.countP(bb, bb1));
        assertEquals(2, ArrayHelper.countP(bb, bb2));
        assertEquals(2, ArrayHelper.countP(bb, bb3));
        assertEquals(1, ArrayHelper.countP(bb, bb4));
        assertEquals(0, ArrayHelper.countP(bb, bb5));
        assertEquals(0, ArrayHelper.countP(bb, bb6));
        assertEquals(3, ArrayHelper.count(bb, bb1));
        assertEquals(2, ArrayHelper.count(bb, bb2));
        assertEquals(2, ArrayHelper.count(bb, bb3));
        assertEquals(1, ArrayHelper.count(bb, bb4));
        assertEquals(0, ArrayHelper.count(bb, bb5));
        assertEquals(0, ArrayHelper.count(bb, bb6));

        bb.position(5);
        assertEquals(2, ArrayHelper.countP(bb, test1));
        assertEquals(1, ArrayHelper.countP(bb, test2));
        assertEquals(1, ArrayHelper.countP(bb, test3));
        assertEquals(1, ArrayHelper.countP(bb, test4));
        assertEquals(0, ArrayHelper.countP(bb, test5));
        assertEquals(0, ArrayHelper.countP(bb, test6));
        assertEquals(3, ArrayHelper.count(bb, test1));
        assertEquals(2, ArrayHelper.count(bb, test2));
        assertEquals(2, ArrayHelper.count(bb, test3));
        assertEquals(1, ArrayHelper.count(bb, test4));
        assertEquals(0, ArrayHelper.count(bb, test5));
        assertEquals(0, ArrayHelper.count(bb, test6));
        assertEquals(2, ArrayHelper.countP(bb, bb1));
        assertEquals(1, ArrayHelper.countP(bb, bb2));
        assertEquals(1, ArrayHelper.countP(bb, bb3));
        assertEquals(1, ArrayHelper.countP(bb, bb4));
        assertEquals(0, ArrayHelper.countP(bb, bb5));
        assertEquals(0, ArrayHelper.countP(bb, bb6));
        assertEquals(3, ArrayHelper.count(bb, bb1));
        assertEquals(2, ArrayHelper.count(bb, bb2));
        assertEquals(2, ArrayHelper.count(bb, bb3));
        assertEquals(1, ArrayHelper.count(bb, bb4));
        assertEquals(0, ArrayHelper.count(bb, bb5));
        assertEquals(0, ArrayHelper.count(bb, bb6));

        bb2.position(1);
        assertEquals(2, ArrayHelper.countP(bb, bb2));
        assertEquals(2, ArrayHelper.count(bb, bb2));
        bb2.limit(1);
        assertEquals(0, ArrayHelper.countP(bb, bb2));
        assertEquals(3, ArrayHelper.count(bb, bb2));
        bb2.position(0);
        assertEquals(2, ArrayHelper.countP(bb, bb2));
        assertEquals(3, ArrayHelper.count(bb, bb2));
    }

    @Test
    public void testEquals() {
        byte[] data1 = { 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6, 6, 5, 4, 3, 2, 1 };
        byte[] data2 = { 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6, 6, 5, 4, 3, 2, 1 };
        ByteBuffer bb1 = ByteBuffer.wrap(data1);
        ByteBuffer bb2 = ByteBuffer.wrap(data2);
        assertTrue(ArrayHelper.equals(bb1, bb2));
        assertTrue(ArrayHelper.equalsP(bb1, bb2));
        bb2.put(0, (byte) -1);
        assertFalse(ArrayHelper.equals(bb1, bb2));
        assertFalse(ArrayHelper.equalsP(bb1, bb2));
        bb2.put(0, (byte) 1);
        bb2.put(bb2.limit() - 1, (byte) -1);
        assertFalse(ArrayHelper.equals(bb1, bb2));
        assertFalse(ArrayHelper.equalsP(bb1, bb2));
        bb2.put(bb2.limit() - 1, (byte) 1);
        assertTrue(ArrayHelper.equals(bb1, bb2));
        assertTrue(ArrayHelper.equalsP(bb1, bb2));

        bb2.position(7);
        assertTrue(ArrayHelper.equals(bb1, bb2));
        assertFalse(ArrayHelper.equalsP(bb1, bb2));
        bb1.position(7);
        assertTrue(ArrayHelper.equalsP(bb1, bb2));
        bb2.limit(12);
        assertFalse(ArrayHelper.equalsP(bb1, bb2));
        bb1.limit(12);
        assertTrue(ArrayHelper.equalsP(bb1, bb2));

        bb1.clear();
        bb2.clear();
        assertTrue(ArrayHelper.equals(bb1, bb2));
        assertTrue(ArrayHelper.equalsP(bb1, bb2));
        bb1.limit(10);
        bb1.position(8);
        bb2.limit(4);
        bb2.position(2);
        assertFalse(ArrayHelper.equals(bb1, bb2));
        assertTrue(ArrayHelper.equalsP(bb1, bb2));
        bb2.limit(9);
        bb2.position(7);
        assertFalse(ArrayHelper.equals(bb1, bb2));
        assertFalse(ArrayHelper.equalsP(bb1, bb2));
        bb1.limit(9);
        assertTrue(ArrayHelper.equals(bb1, bb2));
        assertFalse(ArrayHelper.equalsP(bb1, bb2));
    }

    @Test
    public void testCopy() {
        byte[] data1 = { 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6, 6, 5, 4, 3, 2, 1 };
        ByteBuffer bb1 = ByteBuffer.wrap(data1);
        ByteBuffer bb2 = ByteBuffer.allocate(50);

        ArrayHelper.copy(bb1, bb2);
        assertTrue(ArrayHelper.equals(bb1, bb2));
        bb2 = ByteBuffer.allocate(50);
        ArrayHelper.copyP(bb1, bb2);
        assertTrue(ArrayHelper.equalsP(bb1, bb2));

        bb2 = ByteBuffer.allocate(50);
        bb2.position(10);
        ArrayHelper.copy(bb1, bb2);
        assertTrue(ArrayHelper.equals(bb1, bb2));
        assertFalse(ArrayHelper.equalsP(bb1, bb2));
        bb2 = ByteBuffer.allocate(50);
        bb2.position(10);
        ArrayHelper.copyP(bb1, bb2);
        assertFalse(ArrayHelper.equals(bb1, bb2));
        assertTrue(ArrayHelper.equalsP(bb1, bb2));

        bb2 = ByteBuffer.allocate(50);
        bb1.position(7);
        ArrayHelper.copy(bb1, bb2);
        assertTrue(ArrayHelper.equals(bb1, bb2));
        assertFalse(ArrayHelper.equalsP(bb1, bb2));
        bb2 = ByteBuffer.allocate(50);
        ArrayHelper.copyP(bb1, bb2);
        assertFalse(ArrayHelper.equals(bb1, bb2));
        assertTrue(ArrayHelper.equalsP(bb1, bb2));

        bb2 = ByteBuffer.allocate(50);
        bb1.limit(13);
        bb1.position(7);
        ArrayHelper.copy(bb1, bb2);
        assertTrue(ArrayHelper.equals(bb1, bb2));
        assertFalse(ArrayHelper.equalsP(bb1, bb2));
        bb2 = ByteBuffer.allocate(50);
        ArrayHelper.copyP(bb1, bb2);
        assertFalse(ArrayHelper.equals(bb1, bb2));
        assertTrue(ArrayHelper.equalsP(bb1, bb2));
    }

    @Test
    public void testToString() {
        String test1 = "你好äöüS§مندرين";
        String test2 = "";
        byte[] data3 = { 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6, 6, 5, 4, 3, 2, 1 };
        String test3 = new String(data3, Helper.CHARSET_UTF8);
        byte[] data1 = test1.getBytes(Helper.CHARSET_UTF8);
        byte[] data2 = test2.getBytes(Helper.CHARSET_UTF8);
        ByteBuffer bb1 = ByteBuffer.wrap(data1);
        ByteBuffer bb2 = ByteBuffer.wrap(data2);
        ByteBuffer bb3 = ByteBuffer.wrap(data3);

        assertEquals(test1, ArrayHelper.toString(data1));
        assertEquals(test2, ArrayHelper.toString(data2));
        assertEquals(test3, ArrayHelper.toString(data3));
        assertEquals(test1, ArrayHelper.toString(bb1));
        assertEquals(test2, ArrayHelper.toString(bb2));
        assertEquals(test3, ArrayHelper.toString(bb3));

        bb1.position(3);
        bb3.position(3);
        assertEquals(test1, ArrayHelper.toString(bb1));
        assertEquals(test3, ArrayHelper.toString(bb3));
        assertFalse(test1.equals(ArrayHelper.toStringP(bb1)));
        assertFalse(test3.equals(ArrayHelper.toStringP(bb3)));
        String test = new String(new byte[] { 4, 5, 6, 1, 2, 3, 4, 5, 6, 6, 5, 4, 3, 2, 1 }, Helper.CHARSET_UTF8);
        assertTrue(test.equals(ArrayHelper.toStringP(bb3)));
        test = test1.substring(1);
        assertTrue(test.equals(ArrayHelper.toStringP(bb1)));
    }

    @Test
    public void testCountArray() {
        byte[] test1 = { 1, 4, 7, 8, 6, 2, 3, 6, 9, 0, 6, 3, 1, -1, 5, -8, -22, 46, 8, 1, 0 };
        assertArrayEquals(new int[] { 0, 12, 19 }, ArrayHelper.countArray(test1, 0, test1.length, (byte) 1));
        assertArrayEquals(new int[] { 9, 20 }, ArrayHelper.countArray(test1, 8, test1.length, (byte) 0));
    }

    @Test
    public void testLastIndexOf() {
        byte[] test1 = { 1, 4, 7, 8, 6, 2, 3, 6, 9, 0, 6, 3, 1, -1, 5, -8, -22, 46, 8, 1, 0 };
        assertEquals(19, ArrayHelper.lastIndexOf(test1, 0, test1.length, (byte) 1));
        assertEquals(2, ArrayHelper.lastIndexOf(test1, 0, test1.length, new byte[] { 7, 8, 6 }));
        assertEquals(14, ArrayHelper.lastIndexOf(test1, 5, test1.length - 3, new byte[] { 5, -8, -22 }));
    }

    @Test
    public void testSubstringBetween() {
        byte[] test1 = { 1, 4, 7, 8, 6, 2, 3, 6, 9, 0, 6, 3, 1, -1, 5, -8, -22, 46, 8, 1, 0 };
        ByteBuffer bb = ByteBuffer.allocate(10);
        assertEquals(2, ArrayHelper.substringBetween(test1, 0, test1.length, (byte) 7, (byte) 2, bb, false));
        assertArrayEquals(new byte[] { 8, 6 }, ArrayHelper.toBytes(bb));

        assertEquals(-1, ArrayHelper.substringBetween(test1, 0, test1.length, (byte) 7, (byte) -2, bb, false));

        bb = ByteBuffer.allocate(20);
        assertEquals(0, ArrayHelper.substringBetween(test1, 0, test1.length, (byte) 1, (byte) 5, bb, false));
        assertArrayEquals(new byte[] { 4, 7, 8, 6, 2, 3, 6, 9, 0, 6, 3, 1, -1 }, ArrayHelper.toBytes(bb));

        bb = ByteBuffer.allocate(20);
        assertEquals(12, ArrayHelper.substringBetween(test1, 0, test1.length, (byte) 1, (byte) 5, bb, true));
        assertArrayEquals(new byte[] { -1 }, ArrayHelper.toBytes(bb));
    }

    @Test
    public void testSubstringBetweenArray() {
        byte[] test1 = { 1, 4, 7, 8, 6, 2, 3, 6, 9, 0, 6, 3, 1, -1, 5, -8, -22, 46, 8, 1, 0 };
        ByteBuffer bb = ByteBuffer.allocate(10);
        assertTrue(0 < ArrayHelper.substringBetween(test1, 0, test1.length, new byte[] { 8, 6 }, new byte[] { 0, 6 },
                bb));
        assertArrayEquals(new byte[] { 2, 3, 6, 9 }, ArrayHelper.toBytes(bb));

        assertTrue(0 < ArrayHelper.substringBetween(test1, 5, test1.length, new byte[] { 0, 6, 3 }, new byte[] { -1 },
                bb));
        assertArrayEquals(new byte[] { 1 }, ArrayHelper.toBytes(bb));

        assertTrue(0 == ArrayHelper.substringBetween(test1, 5, test1.length, new byte[] { 6 }, new byte[] { 9 }, bb));

        byte[] test2 = "<title> TEST</title>".getBytes(Helper.CHARSET_UTF8);
        bb.clear();
        assertEquals(4, ArrayHelper.substringBetween(test2, 0, test2.length, "<title>".getBytes(Helper.CHARSET_UTF8),
                "</title>".getBytes(Helper.CHARSET_UTF8), bb));
        assertArrayEquals("TEST".getBytes(Helper.CHARSET_UTF8), ArrayHelper.toBytes(bb));
    }

    @Test
    public void testIndexOf() {
        byte[] test1 = { 1, 4, 7, 8, 6, 2, 3, 6, 9, 0, 6, 3, 1, -1, 5, -8, -22, 46, 8, 1, 0 };
        assertEquals(4, ArrayHelper.indexOf(test1, 0, test1.length, new byte[] { 6, 2, 3, 6 }));
        assertEquals(4, ArrayHelper.indexOf(test1, 2, test1.length - 2, new byte[] { 6, 2, 3, 6 }));
        assertEquals(0, ArrayHelper.indexOf(test1, 0, test1.length, new byte[] { 1 }));
        assertEquals(12, ArrayHelper.indexOf(test1, 2, test1.length - 2, new byte[] { 1 }));
    }

    @Test
    public void testMd5() {
        System.out.println(ArrayHelper.toHexString(ArrayHelper.md5P(ByteBuffer.wrap("Wang_Bo.jpg"
                .getBytes(Helper.CHARSET_UTF8)))));
        assertArrayEquals(new byte[] { (byte) 0x8e, (byte) 0xf9, (byte) 0x3b, (byte) 0x78, (byte) 0xdc, (byte) 0x67,
                (byte) 0x01, (byte) 0x9c, (byte) 0xd2, (byte) 0x63, (byte) 0xd1, (byte) 0x01, (byte) 0x16, (byte) 0xaa,
                (byte) 0x50, (byte) 0x05 },
                ArrayHelper.md5P(ByteBuffer.wrap("Wang_Bo.jpg".getBytes(Helper.CHARSET_UTF8))));
    }
}
