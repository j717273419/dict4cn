package cn.kk.kkdict.beans;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * 
 * This class can be reused by calling parseFrom-method. This class is not thread-safe. ByteBuffer result is temporary
 * and will be changed on eath method call.
 * 
 */
public class DictByteBufferRow {

    private static final int INITIAL_ATTRS_SIZE = 20;
    private static final int INITIAL_VALUES_SIZE = 20;
    private static final int INITIAL_DEFS_SIZE = 200;

    private final static void ensureCapacity(final ArrayList<IntList> list, final int capacity) {
        list.ensureCapacity(capacity);
        for (int i = list.size(); i < capacity; i++) {
            list.add(new IntList(INITIAL_VALUES_SIZE).size(INITIAL_VALUES_SIZE));
        }
    }

    public static final DictByteBufferRow parse(final ByteBuffer rowBB) {
        return parse(rowBB, false);
    }

    public static final DictByteBufferRow parse(final ByteBuffer rowBB, final boolean copy) {
        DictByteBufferRow row = new DictByteBufferRow();
        row.parseFrom(rowBB, copy);
        return row;
    }

    private byte[] array;

    private boolean attrsAnalyzed;
    private boolean valuesSorted;
    private ArrayList<IntList> attrsSize = new ArrayList<IntList>(INITIAL_DEFS_SIZE);
    private ArrayList<IntList> attrsStartIdx = new ArrayList<IntList>(INITIAL_DEFS_SIZE);
    private ArrayList<IntList> attrsStopIdx = new ArrayList<IntList>(INITIAL_DEFS_SIZE);
    private ArrayList<ArrayList<IntList>> attrStartIdx = new ArrayList<ArrayList<IntList>>(INITIAL_DEFS_SIZE);
    private ArrayList<ArrayList<IntList>> attrStopIdx = new ArrayList<ArrayList<IntList>>(INITIAL_DEFS_SIZE);
    private ByteBuffer bb;
    private IntList defStartIdx = new IntList(INITIAL_DEFS_SIZE);
    private IntList defStopIdx = new IntList(INITIAL_DEFS_SIZE);
    private IntList lngStartIdx = new IntList(INITIAL_DEFS_SIZE);
    private IntList lngStopIdx = new IntList(INITIAL_DEFS_SIZE);
    private IntList valuesSize = new IntList(INITIAL_DEFS_SIZE);
    private int size;
    private ArrayList<IntList> valueStartIdx = new ArrayList<IntList>(INITIAL_DEFS_SIZE);
    private ArrayList<IntList> valueStopIdx = new ArrayList<IntList>(INITIAL_DEFS_SIZE);
    // original limit of bb
    private int originalLimit;
    private int originalPosition;
    // (modified) limit of bb modified to fit to the definitions line
    private int actualLimit;
    private int actualPosition;
    private boolean copied;

    public DictByteBufferRow() {
    }

    public void debug(final int i) {
        System.out.println("DictByteBufferRow [size=" + size + ", attrsAnalyzed=" + attrsAnalyzed + ", defStartIdx="
                + defStartIdx.get(i) + ", defStopIdx=" + defStopIdx.get(i) + ", lngStartIdx=" + lngStartIdx.get(i)
                + ", lngStopIdx=" + lngStopIdx.get(i) + ", valueStartIdx=" + valueStartIdx.get(i).get(0)
                + ", valueStopIdx=" + valueStopIdx.get(i).get(0) + "]");
    }

    public boolean equals(final DictByteBufferRow other) {
        if (this == other) {
            return true;
        }
        if (size != other.size) {
            return false;
        }
        if (bb == other.bb) {
            // other.bb should be based on another byte buffer
            return true;
        }
        int defIdx;
        for (int defIdx2 = 0; defIdx2 < size; defIdx2++) {
            defIdx = indexOfLanguage(other.getLanguage(defIdx2));
            if (defIdx == -1) {
                return false;
            } else {
                final int valSize = getValueSize(defIdx);
                final int valSize2 = other.getValueSize(defIdx2);
                if (valSize != valSize2) {
                    return false;
                }
                if (valuesSorted && other.valuesSorted) {
                    // whole definition must be same
                    getDefinitionWithAttributes(defIdx);
                    other.getDefinitionWithAttributes(defIdx2);
                    if (bb.hasRemaining() && other.bb.hasRemaining() && ArrayHelper.equalsP(bb, other.bb)) {
                        continue;
                    } else {
                        return false;
                    }
                } else {
                    // compare value by value
                    for (int valIdx2 = 0; valIdx2 < valSize2; valIdx2++) {
                        final int valIdx = indexOfValue(defIdx, other.getValue(defIdx2, valIdx2));
                        getValueWithAttributes(defIdx, valIdx);
                        other.getValueWithAttributes(defIdx2, valIdx2);
                        if (bb.hasRemaining() && other.bb.hasRemaining() && ArrayHelper.equalsP(bb, other.bb)) {
                            continue;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public final int indexOfValue(final int defIdx, ByteBuffer valBB) {
        if (bb == valBB) {
            valBB = ByteBuffer.wrap(ArrayHelper.toBytesP(valBB));
        }
        for (int valIdx = 0; valIdx < size; valIdx++) {
            getValue(defIdx, valIdx);
            if (bb.hasRemaining() && ArrayHelper.equalsP(bb, valBB)) {
                return valIdx;
            }
        }
        bb.limit(0);
        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DictByteBufferRow other = (DictByteBufferRow) obj;
        return equals(other);
    }

    @Override
    protected void finalize() throws Throwable {
        if (copied) {
            ArrayHelper.giveBack(bb);
        }
        super.finalize();
    }

    public final ByteBuffer getFirstValueAttribute(final int defIdx, final int attrIdx) {
        return getAttribute(defIdx, 0, attrIdx);
    }

    public final ByteBuffer getAttribute(final int defIdx, final int valIdx, final int attrIdx) {
        if (defIdx < size) {
            if (!attrsAnalyzed) {
                parseAttributes();
            }
            final int asSize = attrsSize.get(defIdx).get(valIdx);
            if (valIdx < asSize) {
                final int aStartIdx = attrStartIdx.get(defIdx).get(valIdx).get(attrIdx);
                final int aStopIdx = attrStopIdx.get(defIdx).get(valIdx).get(attrIdx);
                bb.limit(aStopIdx);
                bb.position(aStartIdx);
            } else {
                bb.limit(0);
            }
        } else {
            bb.limit(0);
        }
        return bb;
    }

    public final ByteBuffer getFirstValueAttributes(final int defIdx) {
        return getAttributes(defIdx, 0);
    }

    public final ByteBuffer getAttributes(final int defIdx, final int valIdx) {
        if (defIdx < size) {
            bb.limit(attrsStopIdx.get(defIdx).get(valIdx));
            bb.position(attrsStartIdx.get(defIdx).get(valIdx));
        } else {
            bb.limit(0);
        }
        return bb;
    }

    public final int getFirstValueAttributesSize(final int defIdx) {
        return getAttributesSize(defIdx, 0);
    }

    public final int getAttributesSize(final int defIdx, final int valIdx) {
        if (defIdx < size) {
            if (!attrsAnalyzed) {
                parseAttributes();
            }
            return attrsSize.get(defIdx).get(valIdx);
        } else {
            return 0;
        }
    }

    /**
     * 
     * @return internal bytebuffer (modified by last request)
     */
    public final ByteBuffer lastResult() {
        return bb;
    }

    /**
     * 
     * @return bytebuffer of whole row (may smaller than original)
     */
    public final ByteBuffer getByteBuffer() {
        bb.limit(actualLimit).position(actualPosition);
        return bb;
    }

    /**
     * 
     * @return original unmodified byte buffer
     */
    public final ByteBuffer byteBuffer() {
        bb.limit(originalLimit).position(originalPosition);
        return bb;
    }

    public final ByteBuffer getDefinitionTo(final int defIdx, final int valIdx) {
        if (defIdx < size) {
            final int end = Math.max(valueStopIdx.get(defIdx).get(valIdx), attrsStopIdx.get(defIdx).get(valIdx));
            bb.limit(end);
            bb.position(defStartIdx.get(defIdx));
        } else {
            bb.limit(0);
        }
        return bb;
    }

    public final ByteBuffer getDefinitionWithAttributes(final int i) {
        if (i < size) {
            bb.limit(defStopIdx.get(i));
            bb.position(defStartIdx.get(i));
        } else {
            bb.limit(0);
        }
        return bb;
    }

    /**
     * 
     * @param lngBB
     * 
     * @return
     */
    public final int indexOfLanguage(ByteBuffer lngBB) {
        if (bb == lngBB) {
            lngBB = ByteBuffer.wrap(ArrayHelper.toBytesP(lngBB));
        }
        for (int i = 0; i < size; i++) {
            getLanguage(i);
            if (bb.hasRemaining() && ArrayHelper.equalsP(bb, lngBB)) {
                return i;
            }
        }
        bb.limit(0);
        return -1;
    }

    public final ByteBuffer getLanguage(final int i) {
        if (i < size) {
            bb.limit(lngStopIdx.get(i));
            bb.position(lngStartIdx.get(i));
        } else {
            bb.limit(0);
        }
        return bb;
    }

    public final ByteBuffer getFirstValue(final int defIdx) {
        return getValue(defIdx, 0);
    }

    public final int getValueSize(final int defIdx) {
        if (defIdx < size) {
            return valuesSize.get(defIdx);
        }
        return 0;
    }

    public final ByteBuffer getValue(final int defIdx, final int valIdx) {
        if (defIdx < size) {
            // System.out.println("start: " + valueStartIdx.get(i) + ", stop:" +
            // valueStopIdx.get(i));
            bb.limit(valueStopIdx.get(defIdx).get(valIdx));
            bb.position(valueStartIdx.get(defIdx).get(valIdx));
        } else {
            bb.limit(0);
        }
        return bb;
    }

    public final ByteBuffer getFirstValueWithAttributes(final int defIdx) {
        return getValueWithAttributes(defIdx, 0);
    }

    public final ByteBuffer getValueWithAttributes(final int defIdx, final int valIdx) {
        if (defIdx < size) {
            bb.limit(Math.max(valueStopIdx.get(defIdx).get(valIdx), attrsStopIdx.get(defIdx).get(valIdx)));
            bb.position(valueStartIdx.get(defIdx).get(valIdx));
        } else {
            bb.limit(0);
        }
        return bb;
    }

    public final boolean hasFirstValueAttribute(final int defIdx, ByteBuffer attrBB) {
        return hasAttribute(defIdx, 0, attrBB);
    }

    public final boolean hasAttribute(final int defIdx, final int valIdx, ByteBuffer attrBB) {
        if (bb == attrBB) {
            attrBB = ByteBuffer.wrap(ArrayHelper.toBytesP(attrBB));
        }
        if (defIdx < size) {
            final int asSize = getAttributesSize(defIdx, valIdx);
            for (int attrIdx = 0; attrIdx < asSize; attrIdx++) {
                getAttribute(defIdx, valIdx, attrIdx);
                if (bb.hasRemaining() && ArrayHelper.equalsP(bb, attrBB)) {
                    return true;
                }
            }
        }
        return false;
    }

    public final boolean hasFirstValueAttributes(final int defIdx) {
        return hasAttributes(defIdx, 0);
    }

    public final boolean hasAttributes(final int defIdx, final int valIdx) {
        if (defIdx < size) {
            if (!attrsAnalyzed) {
                parseAttributes();
            }
            return attrsSize.get(defIdx).get(valIdx) > 0;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(array);
        result = prime * result + size;
        return result;
    }

    public final boolean isEmpty() {
        return size <= 0;
    }

    public boolean isLinkedBy(final DictByteBufferRow other) {
        if (!isEmpty() && !other.isEmpty()) {
            int defIdx;
            for (int defIdx2 = 0; defIdx2 < other.size; defIdx2++) {
                defIdx = indexOfLanguage(other.getLanguage(defIdx2));
                if (defIdx != -1) {
                    final int valSize = getValueSize(defIdx);
                    final int valSize2 = other.getValueSize(defIdx2);
                    for (int valIdx = 0; valIdx < valSize; valIdx++) {
                        for (int valIdx2 = 0; valIdx2 < valSize2; valIdx2++) {
                            if (ArrayHelper.equalsP(getValue(defIdx, valIdx), other.getValue(defIdx2, valIdx2))) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * parse one line (without part seperator, new lines etc.), bytebuffer must start with definition
     */
    private DictByteBufferRow parse() {
        actualPosition = DictHelper.getNextStartPoint(array, originalPosition, originalLimit);
        actualLimit = DictHelper.getNextStopPoint(array, actualPosition, originalLimit, DictHelper.ORDER_PARTS);
        bb.position(actualPosition);
        bb.limit(actualLimit);

        if (ArrayHelper.sizeP(bb) > Helper.SEP_DEFINITION_BYTES.length) {
            size = ArrayHelper.countP(bb, Helper.SEP_LIST_BYTES) + 1;
        } else {
            size = 0;
        }
        defStartIdx.size(size);
        defStopIdx.size(size);
        lngStartIdx.size(size);
        lngStopIdx.size(size);
        valuesSize.size(size);
        ensureCapacity(valueStartIdx, size);
        ensureCapacity(valueStopIdx, size);
        ensureCapacity(attrsSize, size);
        ensureCapacity(attrsStartIdx, size);
        ensureCapacity(attrsStopIdx, size);

        if (size > 0) {
            final int start = bb.position();
            final int end = bb.limit();

            int defIdx = 0;
            int valIdx = 0;
            defStartIdx.set(defIdx, start);
            lngStartIdx.set(defIdx, start);

            byte b;
            int sepDefIdx = 0;
            int sepListIdx = 0;
            int sepAttrIdx = 0;
            int sepWordIdx = 0;
            boolean sepDefFound = false;
            boolean sepAttrFound = false;
            for (int i = start; i < end; i++) {
                b = array[i];
                if (!sepDefFound) {
                    if (b == Helper.SEP_DEFINITION_BYTES[sepDefIdx]) {
                        if (++sepDefIdx >= Helper.SEP_DEFINITION_BYTES.length) {
                            // value start
                            lngStopIdx.set(defIdx, i - Helper.SEP_DEFINITION_BYTES.length + 1);
                            valueStartIdx.get(defIdx).set(valIdx, i + 1);
                            sepDefFound = true;
                            sepDefIdx = 0;
                        }
                    } else {
                        sepDefIdx = 0;
                    }
                } else {
                    if (!sepAttrFound) {
                        if (b == Helper.SEP_ATTRS_BYTES[sepAttrIdx]) {
                            if (++sepAttrIdx >= Helper.SEP_ATTRS_BYTES.length) {
                                // attrs start
                                valueStopIdx.get(defIdx).set(valIdx, i - Helper.SEP_ATTRS_BYTES.length + 1);
                                attrsStartIdx.get(defIdx).set(valIdx, i + 1);
                                sepAttrFound = true;
                                sepAttrIdx = 0;
                            }
                        } else {
                            sepAttrIdx = 0;
                        }
                    }
                    if (b == Helper.SEP_WORDS_BYTES[sepWordIdx]) {
                        if (++sepWordIdx >= Helper.SEP_WORDS_BYTES.length) {
                            // next word start
                            final int stopIdx = i - Helper.SEP_WORDS_BYTES.length + 1;
                            valueStopIdx.get(defIdx).set(valIdx, stopIdx);
                            if (sepAttrFound) {
                                attrsStopIdx.get(defIdx).set(valIdx, stopIdx);
                            } else {
                                // no attributes
                                attrsStartIdx.get(defIdx).set(valIdx, 0);
                                attrsStopIdx.get(defIdx).set(valIdx, 0);
                            }
                            valIdx++;
                            final int nextStartIdx = i + 1;
                            valueStartIdx.get(defIdx).set(valIdx, nextStartIdx);
                            sepAttrFound = false;
                            sepWordIdx = 0;
                            sepAttrIdx = 0;
                        }
                    } else {
                        sepWordIdx = 0;
                    }
                    if (b == Helper.SEP_LIST_BYTES[sepListIdx]) {
                        if (++sepListIdx >= Helper.SEP_LIST_BYTES.length) {
                            final int stopIdx = i - Helper.SEP_LIST_BYTES.length + 1;
                            // next def start
                            defStopIdx.set(defIdx, stopIdx);
                            if (sepAttrFound) {
                                attrsStopIdx.get(defIdx).set(valIdx, stopIdx);
                            } else {
                                valueStopIdx.get(defIdx).set(valIdx, stopIdx);
                                // no attributes
                                attrsStartIdx.get(defIdx).set(valIdx, 0);
                                attrsStopIdx.get(defIdx).set(valIdx, 0);
                            }
                            valuesSize.set(defIdx, valIdx + 1);
                            defIdx++;
                            final int nextStartIdx = i + 1;
                            defStartIdx.set(defIdx, nextStartIdx);
                            lngStartIdx.set(defIdx, nextStartIdx);
                            sepDefFound = false;
                            sepAttrFound = false;
                            sepListIdx = 0;
                            sepAttrIdx = 0;
                            sepDefIdx = 0;
                            sepWordIdx = 0;
                            valIdx = 0;
                        }
                    } else {
                        sepListIdx = 0;
                    }
                }
            }
            // last end
            defStopIdx.set(defIdx, end);
            valuesSize.set(defIdx, valIdx + 1);
            if (sepAttrFound) {
                attrsStopIdx.get(defIdx).set(valIdx, end);
            } else {
                valueStopIdx.get(defIdx).set(valIdx, end);
                attrsStartIdx.get(defIdx).set(valIdx, 0);
                attrsStopIdx.get(defIdx).set(valIdx, 0);
            }
        }
        attrsAnalyzed = false;
        valuesSorted = false;
        return this;
    }

    private void parseAttributes() {
        if (!attrsAnalyzed) {
            // ensure capacity
            attrStartIdx.ensureCapacity(size);
            attrStopIdx.ensureCapacity(size);
            for (int i = attrStartIdx.size(); i < size; i++) {
                attrStartIdx.add(new ArrayList<IntList>(INITIAL_VALUES_SIZE));
                attrStopIdx.add(new ArrayList<IntList>(INITIAL_VALUES_SIZE));
            }

            for (int defIdx = 0; defIdx < size; defIdx++) {
                final int valSize = getValueSize(defIdx);
                // ensure capacity
                final ArrayList<IntList> attrStartDefIdx = attrStartIdx.get(defIdx);
                final ArrayList<IntList> attrStopDefIdx = attrStopIdx.get(defIdx);
                attrStartDefIdx.ensureCapacity(valSize);
                attrStopDefIdx.ensureCapacity(valSize);
                for (int i = attrStartDefIdx.size(); i < size; i++) {
                    attrStartDefIdx.add(new IntList(INITIAL_ATTRS_SIZE).size(INITIAL_ATTRS_SIZE));
                    attrStopDefIdx.add(new IntList(INITIAL_ATTRS_SIZE).size(INITIAL_ATTRS_SIZE));
                }

                for (int valIdx = 0; valIdx < valSize; valIdx++) {
                    getAttributes(defIdx, valIdx);
                    final IntList attrStartValIdx = attrStartDefIdx.get(valIdx);
                    final IntList attrStopValIdx = attrStopDefIdx.get(valIdx);
                    if (bb.limit() == 0) {
                        attrStartValIdx.clear();
                        attrStopValIdx.clear();
                    } else {
                        final int s = ArrayHelper.countP(bb, Helper.SEP_ATTRS_BYTES) + 1;
                        attrsSize.get(defIdx).set(valIdx, s);
                        attrStartValIdx.size(s);
                        attrStopValIdx.size(s);

                        int start = bb.position();
                        int end = bb.limit();

                        int attrIdx = 0;
                        attrStartValIdx.set(attrIdx, start);
                        byte b;
                        int sepIdx = 0;
                        for (int i = start; i < end; i++) {
                            b = array[i];
                            if (b == Helper.SEP_ATTRS_BYTES[sepIdx]) {
                                if (++sepIdx >= Helper.SEP_ATTRS_BYTES.length) {
                                    // next attribute start
                                    attrStopValIdx.set(attrIdx, i - Helper.SEP_ATTRS_BYTES.length + 1);
                                    attrIdx++;
                                    attrStartValIdx.set(attrIdx, i + 1);
                                    sepIdx = 0;
                                }
                            } else {
                                sepIdx = 0;
                            }
                        }
                        // last attribute
                        attrStopValIdx.set(attrIdx, end);
                    }
                }
            }

            attrsAnalyzed = true;
        }
    }

    public DictByteBufferRow parseFrom(final ByteBuffer rowBB) {
        return parseFrom(rowBB, false);
    }

    public DictByteBufferRow parseFrom(final ByteBuffer rowBB, final boolean copy) {
        if (copy) {
            if (copied) {
                if (bb.capacity() < rowBB.remaining()) {
                    ArrayHelper.giveBack(bb);
                    bb = ArrayHelper.borrowByteBuffer(rowBB.remaining());
                }
            } else {
                bb = ArrayHelper.borrowByteBuffer(rowBB.remaining());
            }
            ArrayHelper.copyP(rowBB, bb);
            copied = true;
        } else {
            if (copied) {
                ArrayHelper.giveBack(bb);
            }
            bb = rowBB;
            copied = false;
        }
        this.array = bb.array();
        originalLimit = bb.limit();
        originalPosition = bb.position();
        return parse();
    }

    public final int size() {
        return size;
    }

    public final int limit() {
        return originalLimit;
    }

    public final int position() {
        return originalPosition;
    }

    public final byte[] array() {
        return array;
    }

    public DictByteBufferRow sortValues() {
        if (!valuesSorted) {
            for (int defIdx = 0; defIdx < size; defIdx++) {
                final int valSize = getValueSize(defIdx);
                // insertion sort
                for (int i = 0; i < valSize; i++) {
                    for (int j = i; j > 0; j--) {
                        final int startIdx1 = valueStartIdx.get(defIdx).get(j - 1);
                        final int startIdx2 = valueStartIdx.get(defIdx).get(j);
                        final int stopIdx1 = valueStopIdx.get(defIdx).get(j - 1);
                        final int stopIdx2 = valueStopIdx.get(defIdx).get(j);
                        if (ArrayHelper.isSuccessor(bb, startIdx1, stopIdx1 - startIdx1, bb, startIdx2, stopIdx2
                                - startIdx2)) {
                            swapValue(defIdx, j, j - 1);
                        } else {
                            break;
                        }
                    }
                }
            }

            valuesSorted = true;
        }
        return this;
    }

    private void swapValue(int defIdx, int valIdx1, int valIdx2) {
        final int start = valueStartIdx.get(defIdx).get(valIdx1);
        final int stop = valueStopIdx.get(defIdx).get(valIdx1);
        final int asStart = attrsStartIdx.get(defIdx).get(valIdx1);
        final int asStop = attrsStopIdx.get(defIdx).get(valIdx1);
        final int asSize = attrsSize.get(defIdx).get(valIdx1);
        final IntList aStart = attrStartIdx.get(defIdx).get(valIdx1);
        final IntList aStop = attrStopIdx.get(defIdx).get(valIdx1);

        valueStartIdx.get(defIdx).set(valIdx1, valueStartIdx.get(defIdx).get(valIdx2));
        valueStopIdx.get(defIdx).set(valIdx1, valueStopIdx.get(defIdx).get(valIdx2));
        attrsStartIdx.get(defIdx).set(valIdx1, attrsStartIdx.get(defIdx).get(valIdx2));
        attrsStopIdx.get(defIdx).set(valIdx1, attrsStopIdx.get(defIdx).get(valIdx2));
        attrsSize.get(defIdx).set(valIdx1, attrsSize.get(defIdx).get(valIdx2));
        attrStartIdx.get(defIdx).set(valIdx1, attrStartIdx.get(defIdx).get(valIdx2));
        attrStopIdx.get(defIdx).set(valIdx1, attrStopIdx.get(defIdx).get(valIdx2));

        valueStartIdx.get(defIdx).set(valIdx2, start);
        valueStopIdx.get(defIdx).set(valIdx2, stop);
        attrsStartIdx.get(defIdx).set(valIdx2, asStart);
        attrsStopIdx.get(defIdx).set(valIdx2, asStop);
        attrsSize.get(defIdx).set(valIdx2, asSize);
        attrStartIdx.get(defIdx).set(valIdx2, aStart);
        attrStopIdx.get(defIdx).set(valIdx2, aStop);
    }

    public String toString(final int defIdx) {
        getDefinitionWithAttributes(defIdx);
        ByteBuffer tmpBB = ArrayHelper.borrowByteBuffer(bb.remaining() * 2);
        tmpBB.put(getLanguage(defIdx));
        tmpBB.put((byte) ' ').put((byte) '=').put((byte) ' ');
        final int valSize = getValueSize(defIdx);
        boolean firstVal = true;
        for (int valIdx = 0; valIdx < valSize; valIdx++) {
            if (firstVal) {
                firstVal = false;
            } else {
                tmpBB.put((byte) ',').put((byte) ' ');
            }
            tmpBB.put(getValue(defIdx, valIdx));
            
            final int asSize = getAttributesSize(defIdx, valIdx);
            if (asSize > 0) {
                tmpBB.put((byte) ' ').put((byte) '(');
                boolean firstAttr = true;
                for (int attrIdx = 0; attrIdx < asSize; attrIdx++) {
                    if (firstAttr) {
                        firstAttr = false;
                    } else {
                        tmpBB.put((byte) ',').put((byte) ' ');
                    }
                    tmpBB.put(getAttribute(defIdx, valIdx, attrIdx));
                }
                tmpBB.put((byte) ')');
            }
            
        }
        tmpBB.limit(tmpBB.position()).rewind();
        String result = ArrayHelper.toStringP(tmpBB); 
        ArrayHelper.giveBack(tmpBB);
        return result;
    }
}
