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
	// TODO
	private String testTw3 = "Müller，拉丁文名稱：Regiomontanus），德國天文学家。 缪勒生於巴伐利亞，年僅13隨即成為萊比錫大學學生，三年後轉往奧地利维也纳大学就讀，為乔治·普尔巴赫的学生，曾经到意大利学习托勒密的天文学。缪勒後來回到德國，在纽伦堡定居下来后，和他的朋友兼赞助人柏那德·瓦尔特（Bernhard Walther）一起进行天文观测。两人一同编印航海历书 …";

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
		assertTrue(ArrayHelper.equalsP(
                ByteBuffer.wrap(testZh2.getBytes(Helper.CHARSET_UTF8)), bb));
		
		bb = ByteBuffer.wrap(testTw3.getBytes(Helper.CHARSET_UTF8));
        ChineseHelper.toSimplifiedChinese(bb);
        assertEquals(ChineseHelper.toSimplifiedChinese(testTw3), ArrayHelper.toString(bb));
//        assertTrue(ArrayHelper.equalsP(
//                ByteBuffer.wrap(testZh3.getBytes(Helper.CHARSET_UTF8)), bb));
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
