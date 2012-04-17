package cn.kk.kkdict.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.junit.Test;

public class ChineseHelperTest {
	private String testZh1 = "不看Ａ片的女人绝不能娶 一般在人们的印象中，一个爱看A片的女人绝不是个好女人，不是灵魂肮脏，就是心理阴暗，不是性欲强烈，就是好色成瘾，不是变态发狂，就是放荡不羁，反正横竖不是个正经货色。";
	private String testTw1 = "不看Ａ片的女人絕不能娶 一般在人們的印象中，一個愛看A片的女人絕不是個好女人，不是靈魂肮臟，就是心理陰暗，不是性欲強烈，就是好色成癮，不是變態發狂，就是放蕩不羈，反正橫豎不是個正經貨色。";
	private String testZh2 = "国§äöüß";
	private String testTw2 = "國§äöüß";

	@Test
	public void testToTraditionalChineseString() {
		assertEquals(testTw1, ChineseHelper.toTraditionalChinese(testZh1));
		assertEquals(testTw2, ChineseHelper.toTraditionalChinese(testZh2));
	}

	@Test
	public void testToSimplifiedChineseString() {
		assertEquals(testZh1, ChineseHelper.toSimplifiedChinese(testTw1));
		assertEquals(testZh2, ChineseHelper.toSimplifiedChinese(testTw2));
	}

	@Test
	public void testToSimplifiedChineseByteBuffer() {
		ByteBuffer bb = ByteBuffer.wrap(testTw1.getBytes(Helper.CHARSET_UTF8));
		ChineseHelper.toSimplifiedChinese(bb);
		assertTrue(ArrayHelper.equalsP(
				ByteBuffer.wrap(testZh1.getBytes(Helper.CHARSET_UTF8)), bb));

		bb = ByteBuffer.wrap(testTw2.getBytes(Helper.CHARSET_UTF8));
		ChineseHelper.toSimplifiedChinese(bb);
	}

	@Test
	public void testToTraditionalChineseByteBuffer() {
		ByteBuffer bb = ByteBuffer.wrap(testZh1.getBytes(Helper.CHARSET_UTF8));
		ChineseHelper.toTraditionalChinese(bb);
		assertTrue(ArrayHelper.equalsP(
				ByteBuffer.wrap(testTw1.getBytes(Helper.CHARSET_UTF8)), bb));

		bb = ByteBuffer.wrap(testZh2.getBytes(Helper.CHARSET_UTF8));
		System.out.println(ArrayHelper.toHexString(bb));
		ChineseHelper.toTraditionalChinese(bb);

		assertEquals(testTw2, ArrayHelper.toStringP(bb));

	}

	@Test
	public void testContainsChinese() {
		assertTrue(ChineseHelper.containsChinese(testZh1));
		assertTrue(ChineseHelper.containsChinese(testZh2));
		assertTrue(ChineseHelper.containsChinese(testTw1));
		assertTrue(ChineseHelper.containsChinese(testTw2));

		assertFalse(ChineseHelper.containsChinese("abcde"));
		assertFalse(ChineseHelper
				.containsChinese("äöü§?éí^_!\"§%&/()=?`*';µ€@'"));
	}

}
