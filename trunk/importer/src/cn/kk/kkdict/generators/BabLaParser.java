package cn.kk.kkdict.generators;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.types.UriLocation;
import cn.kk.kkdict.utils.Helper;

public class BabLaParser {
	private static final String		URL			= "http://en.bab.la/dictionary/";

	private static final String		OUTDIR	= Configuration.IMPORTER_FOLDER_RAW_WORDS.getPath(Source.WORD_BABLA);

	private static final String		PREFIX	= "words_";

	private static final boolean	DEBUG		= false;

	public static void main(String[] args) throws IOException {
		BabLaParser parser = new BabLaParser();
		List<String[]> available = parser.parseAvailableTranslations();
		for (String[] entry : available) {
			if (DEBUG) {
				System.out.println(entry[0] + "->" + entry[1] + ": " + entry[2]);
			}
			parser.parseTranslationList(entry[0], entry[1], URL + "/" + entry[2]);
		}
	}

	private void parseTranslationList(final String from, final String to, String url) throws IOException {
		// map lng
		final Language lngFrom = getLanguage(from);
		final Language lngTo = getLanguage(to);
		// write file
		final String file = OUTDIR + "/" + PREFIX + lngFrom.key + "_" + lngTo.key + "." + TranslationSource.BABLA.key;
		if (Helper.isEmptyOrNotExists(file)) {
			final long start = System.currentTimeMillis();
			System.out.print("创建文件：" + file + " 。。。 ");
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file), Helper.BUFFER_SIZE);
			long total = 0;
			for (char c = 'a'; c <= 'z'; c++) {
				int pageNr = 1;
				while (true) {
					int words = 0;
					final BufferedReader reader = new BufferedReader(new InputStreamReader(Helper.openUrlInputStream(url + c + "/" + pageNr), Helper.CHARSET_UTF8));
					String line;
					while (null != (line = reader.readLine())) {
						if (line.contains("<div class=\"result-wrapper\">")) {
							int nextHrefStart = 0;
							int nextHrefClose = 0;
							int nextAnchorClose = 0;
							while (true) {
								nextHrefStart = line.indexOf("href=\"", nextAnchorClose);
								if (nextHrefStart != -1) {
									nextHrefStart += "href=\"".length();
									nextHrefClose = line.indexOf("\">", nextHrefStart);
									final String wordUrl = line.substring(nextHrefStart, nextHrefClose);
									int nextAnchorStart = nextHrefClose + "\">".length();
									nextAnchorClose = line.indexOf("</a>", nextAnchorStart);
									if (nextAnchorStart != -1 && nextAnchorClose != -1) {
										try {
											final String substring = line.substring(nextAnchorStart, nextAnchorClose);
											final String word = substring.trim();
											out.write(lngFrom.keyBytes);
											out.write(Helper.SEP_DEFINITION_BYTES);
											out.write(word.getBytes(Helper.CHARSET_UTF8));
											out.write(Helper.SEP_ATTRS_BYTES);
											out.write(UriLocation.TYPE_ID_BYTES);
											out.write(wordUrl.getBytes(Helper.CHARSET_UTF8));
											out.write(Helper.SEP_NEWLINE_BYTES);
											if (DEBUG) {
												System.out.println("新词：" + word + "，网址：" + wordUrl);
											}
											words++;
										} catch (RuntimeException e) {
											System.err.println(e.toString());
										}
										continue;
									}
								}
								break;
							}
						}
					}
					reader.close();
					if (words == 0) {
						break;
					}
					total += words;
					pageNr++;
				}
			}
			System.out.println("共" + total + "词组，花时：" + Helper.formatDuration(System.currentTimeMillis() - start));
			out.close();
		} else {
			System.out.println("跳过：" + file + "，文件已存在。");
		}
	}

	private final static Language getLanguage(final String lng) {
		if (lng.equals("cn")) {
			return Language.ZH;
		} else {
			return Language.fromKey(lng);
		}
	}

	private List<String[]> parseAvailableTranslations() throws IOException {
		final List<String[]> available = new LinkedList<String[]>();
		final BufferedReader reader = new BufferedReader(new InputStreamReader(Helper.openUrlInputStream(URL), Helper.CHARSET_UTF8));
		String line;
		while (null != (line = reader.readLine())) {
			if (line.contains("</span><span class=\"babFlag ")) {
				int nextSpanClose = 0;
				int lastSpanStart;
				int nextHrefStart;
				int nextHrefClose;

				while (true) {
					nextSpanClose = line.indexOf("</span>", nextSpanClose + 1);
					if (nextSpanClose != -1) {
						lastSpanStart = line.lastIndexOf(">", nextSpanClose) + 1;
						String from = line.substring(lastSpanStart, nextSpanClose);
						nextSpanClose = line.indexOf("</span>", nextSpanClose + 1);
						if (nextSpanClose != -1) {
							lastSpanStart = line.lastIndexOf(">", nextSpanClose) + 1;
							String to = line.substring(lastSpanStart, nextSpanClose);
							nextHrefStart = line.indexOf("href=\"", nextSpanClose + 1);
							if (nextHrefStart != -1) {
								nextHrefStart += "href=\"".length();
								nextHrefClose = line.indexOf("\">", nextHrefStart);
								String url = line.substring(nextHrefStart, nextHrefClose);
								if (DEBUG) {
									System.out.println(from + "->" + to + ": " + url);
								}
								available.add(new String[] { from, to, url });
								continue;
							}
						}
					}
					break;
				}
			}
		}
		reader.close();
		return available;
	}
}