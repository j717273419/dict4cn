package cn.kk.kkdict.utils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.kk.kkdict.beans.DictByteBufferRow;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.TranslationSource;

public final class DictHelper {
    private static final boolean DEBUG = false;
    public static final int ORDER_ATTRIBUTE = 4;
    public static final int ORDER_LIST = 3;
    public static final int ORDER_NEWLINE = 1;
    public static final int ORDER_PARTS = 2;
    public static final byte[] SEP_ATTR_TRANSLATION_SRC_BYTES = (Helper.SEP_ATTRIBUTE + TranslationSource.TYPE_ID)
            .getBytes(Helper.CHARSET_UTF8);

    public static final Language[] TOP_LANGUAGES = { Language.EN, Language.DE, Language.FR, Language.NL, Language.NL,
            Language.IT, Language.PL, Language.ES, Language.RU, Language.JA, Language.PT, Language.SV, Language.VI,
            Language.UK, Language.CA, Language.NO, Language.FI, Language.CS, Language.HU, Language.KO, Language.ID,
            Language.TR };

    /**
     * 
     * @param bb
     * @param lngDefBytes
     * @param includeDef
     * @return absolute position index
     */
    public static final int positionSortLng(final ByteBuffer bb, final byte[] lngDefBytes, final boolean includeDef) {
        final byte[] array = bb.array();
        final int endIdx = DictHelper.getStopPoint(array, bb.position(), bb.limit(), DictHelper.ORDER_PARTS);
        int lngIdx = bb.position() - 1;
        byte c;
        do {
            c = -1;
            lngIdx = ArrayHelper.indexOf(array, lngIdx + 1, endIdx - (lngIdx + 1), lngDefBytes);
            if (lngIdx >= 0) {
                if (lngIdx == 0) {
                    c = -1;
                } else {
                    c = array[lngIdx - 1];
                }
            }
        } while (c >= 'a' && c <= 'z');
        if (lngIdx == -1) {
            bb.position(endIdx);
            return -1;
        } else {
            if (includeDef) {
                bb.position(lngIdx);
            } else {
                bb.position(lngIdx + lngDefBytes.length);
            }
            return bb.position();
        }
    }

    /**
     * 
     * @param array
     * @param start
     *            absolute idx
     * @param limit
     *            absolute idx
     * @param innerstSep
     * @return absolute idx of stop separator
     */
    public static final int getStopPoint(byte[] array, final int start, final int limit, final int innerstSep) {
        return getStopPoint(array, start, limit, innerstSep, false);
    }

    /**
     * 
     * @param array
     * @param start
     *            absolute idx
     * @param limit
     *            absolute idx
     * @param innerstSep
     * @param ignoreFirst
     * @return absolute idx of stop separator
     */
    public static final int getStopPoint(byte[] array, final int start, final int limit, final int innerstSep,
            final boolean includeFirst) {
        byte b;
        int l = 0;
        final int lim = Math.min(array.length, limit);
        for (int i = start; i < lim;) {
            b = array[i++];
            if (b == '\n') {
                break;
            } else if ((l != 0 || includeFirst) && b == Helper.SEP_PARTS_BYTES[0] && i + 1 < lim) {
                b = array[i++];
                if (innerstSep >= ORDER_ATTRIBUTE && b == Helper.SEP_ATTRS_BYTES[1] && l + 1 < lim) {
                    b = array[i++];
                    if (b == Helper.SEP_ATTRS_BYTES[2]) {
                        break;
                    } else {
                        l += 3;
                        continue;
                    }
                } else if (innerstSep >= ORDER_LIST && b == Helper.SEP_LIST_BYTES[1] && i + 1 < lim) {
                    b = array[i++];
                    if (b == Helper.SEP_LIST_BYTES[2]) {
                        break;
                    } else {
                        l += 3;
                        continue;
                    }
                } else if (innerstSep >= ORDER_PARTS && b == Helper.SEP_PARTS_BYTES[1] && i + 1 < lim) {
                    b = array[i++];
                    if (b == Helper.SEP_PARTS_BYTES[2]) {
                        break;
                    } else {
                        l += 3;
                        continue;
                    }
                } else {
                    l += 2;
                    continue;
                }
            } else {
                l++;
                continue;
            }
        }
        return start + l;
    }

    /**
     * 
     * @param bb
     * @param innerstSep
     * @return relative length to stop separator
     */
    public static final int getStopPoint(ByteBuffer bb, int innerstSep) {
        int start = bb.position();
        return getStopPoint(bb.array(), start, bb.limit(), innerstSep) - start;
    }

    /**
     * 
     * @param mergeBB
     *            start position is current mergeBB position
     * @param inFileBB
     * @return content will be merged into mergeBB
     */
    public static final int mergeOneDefinitionAndAttributes(final ByteBuffer mergeBB, final ByteBuffer inFileBB) {
        // copy infile line, attribute by attribute
        int attrLen;
        int listLen;
        int idx;
        int mergedPosition = mergeBB.position();
        int inFileStop = DictHelper.getStopPoint(inFileBB, DictHelper.ORDER_PARTS);
        while (inFileBB.position() < inFileStop) {
            // len of first element, next attribute/list/part/nl stop
            attrLen = DictHelper.getStopPoint(inFileBB, DictHelper.ORDER_ATTRIBUTE);
            // next list/part/nl stop
            listLen = DictHelper.getStopPoint(inFileBB, DictHelper.ORDER_LIST);
            // index of definition key in mergeBB
            final byte[] mergeBBArray = mergeBB.array();
            final byte[] inFileBBArray = inFileBB.array();
            idx = ArrayHelper.indexOf(mergeBBArray, 0, mergedPosition, inFileBBArray, inFileBB.position(), attrLen);
            if (-1 == idx) {
                // definition (key) not found in mergeBB -> append definition
                System.arraycopy(Helper.SEP_LIST_BYTES, 0, mergeBBArray, mergedPosition, Helper.SEP_LIST_BYTES.length);
                mergedPosition += Helper.SEP_LIST_BYTES.length;
                System.arraycopy(inFileBBArray, inFileBB.position(), mergeBBArray, mergedPosition, listLen);
                mergedPosition += listLen;
            } else if (listLen - attrLen > Helper.SEP_LIST_BYTES.length
                    && DictHelper.isSeparator(mergeBB, idx + attrLen)) {
                // definition found -> merge attributes
                if (DEBUG) {
                    System.out.println("\nkey: " + ArrayHelper.toString(inFileBBArray, inFileBB.position(), attrLen));
                }
                inFileBB.position(inFileBB.position() + attrLen);
                listLen -= attrLen;
                final int mergeStart = idx + attrLen;
                final int mergeStop = DictHelper.getStopPoint(mergeBBArray, mergeStart, mergedPosition,
                        DictHelper.ORDER_LIST, true);
                while (inFileBB.hasRemaining()) {
                    attrLen = DictHelper.getStopPoint(inFileBB, DictHelper.ORDER_ATTRIBUTE);
                    if (DictHelper.isRelevantAttribute(inFileBB, attrLen)) {
                        idx = ArrayHelper.indexOf(mergeBBArray, mergeStart, mergeStop - mergeStart, inFileBBArray,
                                inFileBB.position(), attrLen);
                        if (DEBUG) {
                            System.out.println("mergebb="
                                    + ArrayHelper.toString(mergeBBArray, mergeStart, mergeStop - mergeStart));
                            System.out
                                    .println("attr="
                                            + ArrayHelper.toString(inFileBB.array(), inFileBB.position(), attrLen)
                                            + ", " + idx);
                        }
                        if (-1 == idx) {
                            System.arraycopy(mergeBBArray, mergeStop, mergeBBArray, mergeStop + attrLen, mergedPosition
                                    - mergeStop);
                            System.arraycopy(inFileBBArray, inFileBB.position(), mergeBBArray, mergeStop, attrLen);
                            mergedPosition += attrLen;
                            if (DEBUG) {
                                System.out.println("merge: "
                                        + ArrayHelper.toString(inFileBB.array(), inFileBB.position(), attrLen));
                            }
                        }
                    }
                    if (listLen - attrLen > Helper.SEP_LIST_BYTES.length) {
                        inFileBB.position(inFileBB.position() + attrLen);
                        listLen -= attrLen;
                    } else {
                        break;
                    }
                }
            }
            // move position pointer to next definition
            inFileBB.position(inFileBB.position() + listLen);
            if (inFileBB.remaining() > Helper.SEP_LIST_BYTES.length) {
                // next begin without list separator
                inFileBB.position(inFileBB.position() + Helper.SEP_LIST_BYTES.length);
            }
        }
        if (mergedPosition > mergeBB.limit()) {
            mergeBB.limit(mergedPosition);
        }
        mergeBB.position(mergedPosition);
        return mergedPosition;
    }

    /**
     * Merge linked definitions and attributes.
     * 
     * @param bb1
     * @param bb2
     * @return merged into bb1
     */
    public static boolean mergeDefinitionsAndAttributes(ByteBuffer bb1, ByteBuffer bb2) {
        return mergeDefinitionsAndAttributes(bb1, bb2, bb1);
    }

    /**
     * Merge linked definitions and attributes.
     * 
     * @param bb1
     * @param bb2
     * @param merged
     * @return merged into merged
     */
    public static boolean mergeDefinitionsAndAttributes(ByteBuffer bb1, ByteBuffer bb2, ByteBuffer merged) {
        DictByteBufferRow row1 = DictByteBufferRow.parse(bb1, true);
        DictByteBufferRow row2 = DictByteBufferRow.parse(bb2, true);
        return mergeDefinitionsAndAttributes(row1, row2, merged);
    }

    /**
     * Merge linked definitions and attributes.
     * 
     * @param row1
     * @param row2
     * @param merged
     * @return
     */
    public static boolean mergeDefinitionsAndAttributes(DictByteBufferRow row1, DictByteBufferRow row2,
            ByteBuffer merged) {
        boolean first = true;
        if (!row1.isEmpty() && !row2.isEmpty() && row1.isLinkedBy(row2) && !row1.equals(row2)) {
            merged.clear();
            int idx;
            for (int i = 0; i < row1.size(); i++) {
                if (first) {
                    first = false;
                } else {
                    merged.put(Helper.SEP_LIST_BYTES);
                }
                merged.put(row1.getDefinitionWithAttributes(i));
                if (-1 != (idx = row2.indexOfLanguage(row1.getLanguage(i)))) {
                    final int attrsSize = row2.getAttributesSize(idx);
                    for (int j = 0; j < attrsSize; j++) {
                        if (!row1.hasAttribute(i, row2.getAttribute(idx, j))) {
                            merged.put(Helper.SEP_ATTRS_BYTES);
                            merged.put(row2.getByteBuffer());
                        }
                    }
                }
            }
            for (int i = 0; i < row2.size(); i++) {
                if (-1 == (idx = row1.indexOfLanguage(row2.getLanguage(i)))) {
                    if (first) {
                        first = false;
                    } else {
                        merged.put(Helper.SEP_LIST_BYTES);
                    }
                    merged.put(row2.getDefinitionWithAttributes(i));
                }
            }
            merged.limit(merged.position()).position(0);
            return true;
        }
        return false;
    }

    /**
     * 
     * @param bb
     * @param limit
     *            relative len to bb.position()
     * @return
     */
    private static boolean isRelevantAttribute(final ByteBuffer bb, final int limit) {
        if (limit > Helper.SEP_ATTRS_BYTES.length) {
            final int pos = bb.position() + Helper.SEP_ATTRS_BYTES.length;
            final int lim = limit - Helper.SEP_ATTRS_BYTES.length;
            if (-1 == ArrayHelper.indexOf(bb.array(), pos, lim, TranslationSource.TYPE_ID_BYTES, 0,
                    TranslationSource.TYPE_ID_BYTES.length)) {
                return true;
            }
        }
        return false;
    }

    public static final boolean isSeparator(final ByteBuffer bb, final int idx) {
        return idx == getStopPoint(bb.array(), idx, idx + 3, ORDER_ATTRIBUTE, true);
    }

    public static final int filterAttributes(ByteBuffer mergeBB) {
        int idx;
        mergeBB.rewind();
        while (-1 != (idx = ArrayHelper.indexOf(mergeBB.array(), mergeBB.position(),
                mergeBB.limit() - mergeBB.position(), DictHelper.SEP_ATTR_TRANSLATION_SRC_BYTES))) {
            int stopIdx = DictHelper.getStopPoint(mergeBB.array(), idx
                    + DictHelper.SEP_ATTR_TRANSLATION_SRC_BYTES.length, mergeBB.limit(), DictHelper.ORDER_ATTRIBUTE);
            if (mergeBB.limit() > stopIdx) {
                System.arraycopy(mergeBB.array(), stopIdx, mergeBB.array(), idx, mergeBB.limit() - stopIdx);
            }
            mergeBB.limit(mergeBB.limit() - (stopIdx - idx));
            mergeBB.position(idx);
        }
        return mergeBB.rewind().limit();
    }

    /**
     * Count occurrences in file. Maximum one time each line. This method assumes that the data is separated by list
     * separator or eol.
     * 
     * @param fileBB
     *            status will be cleared in method
     * @param data
     * @param offset
     * @param limit
     *            absolute
     * @return
     * @throws IOException
     */
    public static int countOccurrencesFast(final ByteBuffer fileBB, final byte[] data, final int offset, final int limit) {
        int count = 0;
        final int size = limit - offset;
        fileBB.clear();
        fileBB.position(Helper.SEP_WORDS_BYTES.length);
        byte b;
        int idx = offset;
        int r;
        byte b1 = -1, b2 = -1, b3 = -1;
        boolean found;
        while ((r = fileBB.remaining()) >= size) {
            b = fileBB.get();
            r--;
            if (b == data[idx]) {
                if (++idx == limit) {
                    found = false;
                    // found, check separator
                    if (r >= 3) {
                        b1 = fileBB.get();
                        b2 = fileBB.get();
                        b3 = fileBB.get();
                        r -= 3;
                        if (b1 == '\n'
                                || b1 == '\r'
                                || (b1 == Helper.SEP_WORDS_BYTES[0] && b2 == Helper.SEP_WORDS_BYTES[1] && b3 == Helper.SEP_WORDS_BYTES[2])) {
                            // found
                            found = true;
                        }
                    } else if (r > 0) {
                        b1 = fileBB.get();
                        r -= 1;
                        if (b1 == '\n' || b1 == '\r') {
                            // found
                            found = true;
                        }
                    } else {
                        // found
                        found = true;
                    }
                    if (found) {
                        count++;
                        idx = offset;
                        // next line
                        while (r-- > 0) {
                            b = fileBB.get();
                            if (b == '\n' || b == '\r') {
                                break;
                            }
                        }
                        if (r > 3) {
                            fileBB.get();
                            fileBB.get();
                            fileBB.get();
                        } else {
                            break;
                        }
                    } else {
                        // not fully matched
                        idx = offset;
                    }
                }
            } else {
                idx = offset;
            }
        }
        return count;
    }

    public static final Language getWikiLanguage(String f) {
        String lngStr = Helper.substringAfter(f, ".wiki_");
        if (lngStr == null) {
            lngStr = Helper.substringBetweenNarrow(f, File.separator, "wiki-");
        }
        if (lngStr == null) {
            lngStr = Helper.substringBetweenNarrow(f, File.separator, "wiktionary-");
        }
        if (lngStr != null) {
            return Language.valueOf(Helper.toConstantName(lngStr));
        }
        return null;
    }

    public static final byte[] findNextWord(ByteBuffer lineBB) {
        int start = lineBB.position();
        int end = start;
        int r = lineBB.remaining();
        byte b;
        int idx = 0;
        while (true) {
            if (r == 0) {
                end = lineBB.position();
                break;
            } else {
                b = lineBB.get();
                r--;
                if (b == Helper.SEP_WORDS_BYTES[idx]) {
                    if (++idx < Helper.SEP_WORDS_BYTES.length) {
                        continue;
                    } else {
                        end = lineBB.position() - Helper.SEP_WORDS_BYTES.length;
                        break;
                    }
                } else {
                    idx = 0;
                }
            }
        }
        if (end == start) {
            return null;
        } else {
            byte[] result = new byte[end - start];
            System.arraycopy(lineBB.array(), start, result, 0, end - start);
            return result;
        }
    }

}
