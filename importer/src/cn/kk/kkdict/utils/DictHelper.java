/*  Copyright (c) 2010 Xiaoyun Zhu
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy  
 *  of this software and associated documentation files (the "Software"), to deal  
 *  in the Software without restriction, including without limitation the rights  
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
 *  copies of the Software, and to permit persons to whom the Software is  
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in  
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,  
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN  
 *  THE SOFTWARE.  
 */
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
        final int endIdx = DictHelper.getNextStopPoint(array, bb.position(), bb.limit(), DictHelper.ORDER_PARTS);
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
    public static final int getNextStopPoint(byte[] array, final int start, final int limit, final int innerstSep) {
        return getNextStopPoint(array, start, limit, innerstSep, false);
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
    public static final int getNextStopPoint(byte[] array, final int start, final int limit, final int innerstSep,
            final boolean includeFirst) {
        byte b;
        int l = 0;
        final int lim = Math.min(array.length, limit);
        for (int i = start; i < lim;) {
            b = array[i++];
            if (b == Helper.SEP_NEWLINE_CHAR) {
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
    public static final int getNextStopPoint(ByteBuffer bb, int innerstSep) {
        int start = bb.position();
        return getNextStopPoint(bb.array(), start, bb.limit(), innerstSep) - start;
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
        int inFileStop = DictHelper.getNextStopPoint(inFileBB, DictHelper.ORDER_PARTS);
        while (inFileBB.position() < inFileStop) {
            // len of first element, next attribute/list/part/nl stop
            attrLen = DictHelper.getNextStopPoint(inFileBB, DictHelper.ORDER_ATTRIBUTE);
            // next list/part/nl stop
            listLen = DictHelper.getNextStopPoint(inFileBB, DictHelper.ORDER_LIST);
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
                final int mergeStop = DictHelper.getNextStopPoint(mergeBBArray, mergeStart, mergedPosition,
                        DictHelper.ORDER_LIST, true);
                while (inFileBB.hasRemaining()) {
                    attrLen = DictHelper.getNextStopPoint(inFileBB, DictHelper.ORDER_ATTRIBUTE);
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

    public static boolean mergeDefinitionsAndAttributes(DictByteBufferRow row1, DictByteBufferRow row2,
            ByteBuffer merged) {
        return mergeDefinitionsAndAttributes(row1, row2, merged, false);
    }

    public static boolean mergeDefinitionsAndAttributesLinked(DictByteBufferRow row1, DictByteBufferRow row2,
            ByteBuffer merged) {
        return mergeDefinitionsAndAttributes(row1, row2, merged, true);
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
            ByteBuffer merged, boolean linked) {
        boolean firstDef = true;
        if (!row1.isEmpty() && !row2.isEmpty() && (!linked || row1.isLinkedBy(row2)) && !row1.equals(row2)) {
            merged.clear();
            int defIdx2;
            int valIdx2;
            for (int defIdx1 = 0; defIdx1 < row1.size(); defIdx1++) {
                if (firstDef) {
                    firstDef = false;
                } else {
                    merged.put(Helper.SEP_LIST_BYTES);
                }
                if (-1 != (defIdx2 = row2.indexOfLanguage(row1.getLanguage(defIdx1)))) {
                    // merge same lng
                    merged.put(row1.lastResult());
                    merged.put(Helper.SEP_DEFINITION_BYTES);
                    boolean firstVal = true;
                    final int valSize1 = row1.getValueSize(defIdx1);
                    for (int valIdx1 = 0; valIdx1 < valSize1; valIdx1++) {
                        if (firstVal) {
                            firstVal = false;
                        } else {
                            merged.put(Helper.SEP_WORDS_BYTES);
                        }
                        // copy row1 value with attrs
                        merged.put(row1.getValueWithAttributes(defIdx1, valIdx1));
                        if (-1 != (valIdx2 = row2.indexOfValue(defIdx2, row1.getValue(defIdx1, valIdx1)))) {
                            // merge same value attrs
                            final int attrsSize2 = row2.getAttributesSize(defIdx2, valIdx2);
                            for (int attrIdx2 = 0; attrIdx2 < attrsSize2; attrIdx2++) {
                                if (!row1.hasAttribute(defIdx1, valIdx1, row2.getAttribute(defIdx2, valIdx2, attrIdx2))) {
                                    merged.put(Helper.SEP_ATTRS_BYTES);
                                    merged.put(row2.lastResult());
                                }
                            }
                        }
                    }
                    final int valSize2 = row2.getValueSize(defIdx2);
                    for (valIdx2 = 0; valIdx2 < valSize2; valIdx2++) {
                        // values only in row2
                        if (-1 == (row1.indexOfValue(defIdx1, row2.getValue(defIdx2, valIdx2)))) {
                            if (firstVal) {
                                firstVal = false;
                            } else {
                                merged.put(Helper.SEP_WORDS_BYTES);
                            }
                            merged.put(row2.getValueWithAttributes(defIdx2, valIdx2));
                        }
                    }
                } else {
                    // lng only in row1
                    merged.put(row1.getDefinitionWithAttributes(defIdx1));
                }
            }
            for (defIdx2 = 0; defIdx2 < row2.size(); defIdx2++) {
                // defs only in row2
                if (-1 == (row1.indexOfLanguage(row2.getLanguage(defIdx2)))) {
                    if (firstDef) {
                        firstDef = false;
                    } else {
                        merged.put(Helper.SEP_LIST_BYTES);
                    }
                    merged.put(row2.getDefinitionWithAttributes(defIdx2));
                }
            }
            merged.limit(merged.position()).rewind();
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
        return idx == getNextStopPoint(bb.array(), idx, idx + 3, ORDER_ATTRIBUTE, true);
    }

    public static final int filterAttributes(ByteBuffer mergeBB) {
        int idx;
        mergeBB.rewind();
        while (-1 != (idx = ArrayHelper.indexOf(mergeBB.array(), mergeBB.position(), mergeBB.remaining(),
                DictHelper.SEP_ATTR_TRANSLATION_SRC_BYTES))) {
            int stopIdx = DictHelper.getNextStopPoint(mergeBB.array(), idx
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
                        if (b1 == Helper.SEP_NEWLINE_CHAR
                                || b1 == '\r'
                                || (b1 == Helper.SEP_WORDS_BYTES[0] && b2 == Helper.SEP_WORDS_BYTES[1] && b3 == Helper.SEP_WORDS_BYTES[2])) {
                            // found
                            found = true;
                        }
                    } else if (r > 0) {
                        b1 = fileBB.get();
                        r -= 1;
                        if (b1 == Helper.SEP_NEWLINE_CHAR || b1 == '\r') {
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
                            if (b == Helper.SEP_NEWLINE_CHAR || b == '\r') {
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
            System.out.println("wiki语言：" + Helper.toConstantName(lngStr));
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

    public static final int getNextStartPoint(final byte[] array, final int offset, final int limit) {
        for (int i = offset; i < limit; i++) {
            final byte b = array[i];
            if (b != Helper.SEP_NEWLINE_CHAR || b != '\t' || b != '\0') {
                return i;
            }
        }
        return offset;
    }

}
