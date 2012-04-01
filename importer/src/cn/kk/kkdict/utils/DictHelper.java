package cn.kk.kkdict.utils;

import java.nio.ByteBuffer;

import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.TranslationSource;

public final class DictHelper {
    private static final boolean DEBUG = false;
    public static final int ORDER_ATTRIBUTE = 4;
    public static final int ORDER_LIST = 3;
    public static final int ORDER_NEWLINE = 1;
    public static final int ORDER_PARTS = 2;
    public static final byte[] SEP_ATTRS_BYTES = Helper.SEP_ATTRIBUTE.getBytes(Helper.CHARSET_UTF8);
    public static final byte[] SEP_LIST_BYTES = Helper.SEP_LIST.getBytes(Helper.CHARSET_UTF8);
    public static final byte[] SEP_NEWLINE_BYTES = Helper.SEP_NEWLINE.getBytes(Helper.CHARSET_UTF8);
    public static final byte[] SEP_PARTS_BYTES = Helper.SEP_PARTS.getBytes(Helper.CHARSET_UTF8);

    public static final Language[] TOP_LANGUAGES = { Language.EN, Language.DE, Language.FR, Language.NL, Language.NL,
            Language.IT, Language.PL, Language.ES, Language.RU, Language.JA, Language.PT, Language.SV, Language.VI,
            Language.UK, Language.CA, Language.NO, Language.FI, Language.CS, Language.HU, Language.KO, Language.ID,
            Language.TR };

    /**
     * 
     * @param bb
     * @param includeDef
     * @return absolute position index
     */
    public static final int positionSortLng(final ByteBuffer bb, final byte[] lngDefBytes, final boolean includeDef) {
        int endIdx = DictHelper.getStopPoint(bb, DictHelper.ORDER_PARTS);
        int lngIdx = bb.position() - 1;
        byte c;
        final byte[] array = bb.array();
        do {
            c = -1;
            lngIdx = Helper.indexOf(array, lngIdx + 1, endIdx - lngIdx - 1, lngDefBytes);
            if (lngIdx >= 0) {
                if (lngIdx == 0) {
                    c = 'a';
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
            } else if ((l != 0 || includeFirst) && b == SEP_PARTS_BYTES[0] && i + 1 < lim) {
                b = array[i++];
                if (innerstSep >= ORDER_ATTRIBUTE && b == SEP_ATTRS_BYTES[1] && l + 1 < lim) {
                    b = array[i++];
                    if (b == SEP_ATTRS_BYTES[2]) {
                        break;
                    } else {
                        l += 3;
                        continue;
                    }
                } else if (innerstSep >= ORDER_LIST && b == SEP_LIST_BYTES[1] && i + 1 < lim) {
                    b = array[i++];
                    if (b == SEP_LIST_BYTES[2]) {
                        break;
                    } else {
                        l += 3;
                        continue;
                    }
                } else if (innerstSep >= ORDER_PARTS && b == SEP_PARTS_BYTES[1] && i + 1 < lim) {
                    b = array[i++];
                    if (b == SEP_PARTS_BYTES[2]) {
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

    public static final boolean isEquals(final ByteBuffer bb1, final ByteBuffer bb2) {
        final int l1 = bb1.limit();
        final int l2 = bb2.limit();
        return isEquals(bb1, 0, l1, bb2, 0, l2);
    }

    public static final boolean isEquals(final ByteBuffer bb1, int offset1, int l1, final ByteBuffer bb2, int offset2,
            final int l2) {
        // return v1.equalsIgnoreCase(v2);
        if (l1 == l2) {
            while (l1-- != 0) {
                if (bb1.get(offset1 + l1) != bb2.get(offset2 + l1)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static final boolean isPredessorEquals(final ByteBuffer bb1, final ByteBuffer bb2) {
        final int l1 = bb1.limit();
        final int l2 = bb2.limit();
        return isPredessorEquals(bb1, 0, l1, bb2, 0, l2);
    }

    public static final boolean isPredessorEquals(final ByteBuffer bb1, final int offset1, final int l1,
            final ByteBuffer bb2, final int offset2, final int l2) {
        // return v1.compareToIgnoreCase(v2) <= 0;
        if (l1 <= 0) {
            return false;
        } else if (l2 <= 0) {
            return true;
        }
        return compareTo(bb1.array(), offset1, l1, bb2.array(), offset2, l2) <= 0;
    }

    public static final boolean isSuccessor(final ByteBuffer bb1, final ByteBuffer bb2) {
        final int l1 = bb1.limit();
        final int l2 = bb2.limit();
        return isSuccessor(bb1, 0, l1, bb2, 0, l2);
    }

    public static final boolean isSuccessor(final ByteBuffer bb1, final int offset1, final int l1,
            final ByteBuffer bb2, final int offset2, final int l2) {
        // return v1.compareToIgnoreCase(v2) > 0;
        if (l1 <= 0) {
            return true;
        } else if (l2 <= 0) {
            return false;
        }
        return compareTo(bb1.array(), offset1, l1, bb2.array(), offset2, l2) > 0;
    }

    public static final int compareTo(byte[] bs1, int offset1, int len1, byte[] bs2, int offset2, int len2) {
        int n = Math.min(len1, len2);
        while (offset1 < n) {
            byte c1 = bs1[offset1];
            byte c2 = bs2[offset2];
            if (c1 != c2) {
                return c1 - c2;
            }
            offset1++;
            offset2++;
        }
        return len1 - len2;
    }

    public static void main(String[] args) {
        ByteBuffer mergeBB = ByteBuffer.allocate(100);
        ByteBuffer inFileBB = ByteBuffer.allocate(100);
        mergeBB.put((byte) '1').put(DictHelper.SEP_ATTRS_BYTES).put((byte) 'a').put(DictHelper.SEP_LIST_BYTES);
        mergeBB.put((byte) '2').put(DictHelper.SEP_ATTRS_BYTES).put((byte) 'b').put(DictHelper.SEP_LIST_BYTES);
        mergeBB.put((byte) '3').put(DictHelper.SEP_ATTRS_BYTES).put((byte) 'b').put(DictHelper.SEP_ATTRS_BYTES)
                .put((byte) 'c');

        inFileBB.put((byte) '1').put(DictHelper.SEP_ATTRS_BYTES).put((byte) 'a').put(DictHelper.SEP_ATTRS_BYTES)
                .put((byte) 'b').put(DictHelper.SEP_ATTRS_BYTES).put((byte) 'c').put(DictHelper.SEP_ATTRS_BYTES)
                .put((byte) 'd').put(DictHelper.SEP_LIST_BYTES);
        inFileBB.put((byte) '2').put(DictHelper.SEP_ATTRS_BYTES).put((byte) 'a').put(DictHelper.SEP_ATTRS_BYTES)
                .put((byte) 'b').put(DictHelper.SEP_ATTRS_BYTES).put((byte) 'c').put(DictHelper.SEP_ATTRS_BYTES)
                .put((byte) 'd').put(DictHelper.SEP_LIST_BYTES);
        inFileBB.put((byte) '3').put(DictHelper.SEP_ATTRS_BYTES).put((byte) 'a').put(DictHelper.SEP_ATTRS_BYTES)
                .put((byte) 'b').put(DictHelper.SEP_ATTRS_BYTES).put((byte) 'c').put(DictHelper.SEP_ATTRS_BYTES)
                .put((byte) 'd');
        inFileBB.limit(inFileBB.position());
        inFileBB.rewind();
        mergeDefinitionsAndAttributes(mergeBB, inFileBB);

        System.out.println(Helper.toString(mergeBB.array(), 0, mergeBB.position()));
    }

    public static final int mergeDefinitionsAndAttributes(final ByteBuffer mergeBB, final ByteBuffer inFileBB) {
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
            idx = Helper.indexOf(mergeBBArray, 0, mergedPosition, inFileBBArray, inFileBB.position(), attrLen);
            if (-1 == idx) {
                // definition (key) not found in mergeBB -> append definition
                System.arraycopy(DictHelper.SEP_LIST_BYTES, 0, mergeBBArray, mergedPosition,
                        DictHelper.SEP_LIST_BYTES.length);
                mergedPosition += DictHelper.SEP_LIST_BYTES.length;
                System.arraycopy(inFileBBArray, inFileBB.position(), mergeBBArray, mergedPosition, listLen);
                mergedPosition += listLen;
            } else if (listLen - attrLen > DictHelper.SEP_LIST_BYTES.length
                    && DictHelper.isSeparator(mergeBB, idx + attrLen)) {
                // definition found -> merge attributes
                if (DEBUG) {
                    System.out.println("\nkey: " + Helper.toString(inFileBBArray, inFileBB.position(), attrLen));
                }
                inFileBB.position(inFileBB.position() + attrLen);
                listLen -= attrLen;
                final int mergeStart = idx + attrLen;
                final int mergeStop = DictHelper.getStopPoint(mergeBBArray, mergeStart, mergedPosition,
                        DictHelper.ORDER_LIST, true);
                while (inFileBB.hasRemaining()) {
                    attrLen = DictHelper.getStopPoint(inFileBB, DictHelper.ORDER_ATTRIBUTE);
                    if (DictHelper.isRelevantAttribute(inFileBB, attrLen)) {
                        idx = Helper.indexOf(mergeBBArray, mergeStart, mergeStop - mergeStart, inFileBBArray,
                                inFileBB.position(), attrLen);
                        if (DEBUG) {
                            System.out.println("mergebb="
                                    + Helper.toString(mergeBBArray, mergeStart, mergeStop - mergeStart));
                            System.out.println("attr="
                                    + Helper.toString(inFileBB.array(), inFileBB.position(), attrLen) + ", " + idx);
                        }
                        if (-1 == idx) {
                            System.arraycopy(mergeBBArray, mergeStop, mergeBBArray, mergeStop + attrLen, mergedPosition
                                    - mergeStop);
                            System.arraycopy(inFileBBArray, inFileBB.position(), mergeBBArray, mergeStop, attrLen);
                            mergedPosition += attrLen;
                            if (DEBUG) {
                                System.out.println("merge: "
                                        + Helper.toString(inFileBB.array(), inFileBB.position(), attrLen));
                            }
                        }
                    }
                    if (listLen - attrLen > DictHelper.SEP_LIST_BYTES.length) {
                        inFileBB.position(inFileBB.position() + attrLen);
                        listLen -= attrLen;
                    } else {
                        break;
                    }
                }
            }
            // move position pointer to next definition
            inFileBB.position(inFileBB.position() + listLen);
            if (inFileBB.remaining() > DictHelper.SEP_LIST_BYTES.length) {
                // next begin without list separator
                inFileBB.position(inFileBB.position() + DictHelper.SEP_LIST_BYTES.length);
            }
        }
        mergeBB.position(mergedPosition);
        return mergedPosition;
    }

    /**
     * 
     * @param bb
     * @param limit
     *            relative len to bb.position()
     * @return
     */
    private static boolean isRelevantAttribute(final ByteBuffer bb, final int limit) {
        if (limit > SEP_ATTRS_BYTES.length) {
            final int pos = bb.position() + SEP_ATTRS_BYTES.length;
            final int lim = limit - SEP_ATTRS_BYTES.length;
            if (-1 == Helper.indexOf(bb.array(), pos, lim, TranslationSource.TYPE_ID_BYTES, 0,
                    TranslationSource.TYPE_ID_BYTES.length)) {
                return true;
            }
        }
        return false;
    }

    public static final boolean isSeparator(final ByteBuffer bb, final int idx) {
        return idx == getStopPoint(bb.array(), idx, idx + 3, ORDER_ATTRIBUTE, true);
    }

}
