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
package cn.kk.kkdict.beans;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * 
 * This class can be reused by calling parseFrom-method. This class is not thread-safe. ByteBuffer result is temporary and will be changed on eath method call.
 * 
 */
public class DictByteBufferRow {

  private static final int INITIAL_ATTRS_SIZE  = 20;
  private static final int INITIAL_VALUES_SIZE = 20;
  private static final int INITIAL_DEFS_SIZE   = 200;

  private final static void ensureCapacity(final ArrayList<IntList> list, final int capacity) {
    list.ensureCapacity(capacity);
    for (int i = list.size(); i < capacity; i++) {
      list.add(new IntList(DictByteBufferRow.INITIAL_VALUES_SIZE).size(DictByteBufferRow.INITIAL_VALUES_SIZE));
    }
  }

  public static final DictByteBufferRow parse(final ByteBuffer rowBB) {
    return DictByteBufferRow.parse(rowBB, false);
  }

  public static final DictByteBufferRow parse(final ByteBuffer rowBB, final boolean copy) {
    final DictByteBufferRow row = new DictByteBufferRow();
    row.parseFrom(rowBB, copy);
    return row;
  }

  private byte[]                              array;

  private boolean                             attrsAnalyzed;
  private boolean                             valuesSorted;
  private final ArrayList<IntList>            attrsSize     = new ArrayList<>(DictByteBufferRow.INITIAL_DEFS_SIZE);
  private final ArrayList<IntList>            attrsStartIdx = new ArrayList<>(DictByteBufferRow.INITIAL_DEFS_SIZE);
  private final ArrayList<IntList>            attrsStopIdx  = new ArrayList<>(DictByteBufferRow.INITIAL_DEFS_SIZE);
  private final ArrayList<ArrayList<IntList>> attrStartIdx  = new ArrayList<>(DictByteBufferRow.INITIAL_DEFS_SIZE);
  private final ArrayList<ArrayList<IntList>> attrStopIdx   = new ArrayList<>(DictByteBufferRow.INITIAL_DEFS_SIZE);
  private ByteBuffer                          bb;
  private final IntList                       defStartIdx   = new IntList(DictByteBufferRow.INITIAL_DEFS_SIZE);
  private final IntList                       defStopIdx    = new IntList(DictByteBufferRow.INITIAL_DEFS_SIZE);
  private final IntList                       lngStartIdx   = new IntList(DictByteBufferRow.INITIAL_DEFS_SIZE);
  private final IntList                       lngStopIdx    = new IntList(DictByteBufferRow.INITIAL_DEFS_SIZE);
  private final IntList                       valuesSize    = new IntList(DictByteBufferRow.INITIAL_DEFS_SIZE);
  private int                                 size;
  private final ArrayList<IntList>            valueStartIdx = new ArrayList<>(DictByteBufferRow.INITIAL_DEFS_SIZE);
  private final ArrayList<IntList>            valueStopIdx  = new ArrayList<>(DictByteBufferRow.INITIAL_DEFS_SIZE);
  // original limit of bb
  private int                                 originalLimit;
  private int                                 originalPosition;
  // (modified) limit of bb modified to fit to the definitions line
  private int                                 actualLimit;
  private int                                 actualPosition;
  private boolean                             copied;

  public DictByteBufferRow() {
  }

  public void debug(final int i) {
    System.out.println("DictByteBufferRow [size=" + this.size + ", attrsAnalyzed=" + this.attrsAnalyzed + ", defStartIdx=" + this.defStartIdx.get(i)
        + ", defStopIdx=" + this.defStopIdx.get(i) + ", lngStartIdx=" + this.lngStartIdx.get(i) + ", lngStopIdx=" + this.lngStopIdx.get(i) + ", valueStartIdx="
        + this.valueStartIdx.get(i).get(0) + ", valueStopIdx=" + this.valueStopIdx.get(i).get(0) + "]");
  }

  public boolean equals(final DictByteBufferRow other) {
    if (this == other) {
      return true;
    }
    if (this.size != other.size) {
      return false;
    }
    if (this.bb == other.bb) {
      // other.bb should be based on another byte buffer
      return true;
    }
    int defIdx;
    for (int defIdx2 = 0; defIdx2 < this.size; defIdx2++) {
      defIdx = this.indexOfLanguage(other.getLanguage(defIdx2));
      if (defIdx == -1) {
        // lng not found
        return false;
      } else {
        final int valSize = this.getValueSize(defIdx);
        final int valSize2 = other.getValueSize(defIdx2);
        if (valSize != valSize2) {
          // val size not equal
          return false;
        }
        if (this.valuesSorted && other.valuesSorted) {
          // whole definition must be same
          this.getDefinitionWithAttributes(defIdx);
          other.getDefinitionWithAttributes(defIdx2);
          if (this.bb.hasRemaining() && other.bb.hasRemaining() && ArrayHelper.equalsP(this.bb, other.bb)) {
            continue;
          } else {
            // val not equal
            return false;
          }
        } else {
          // compare value by value
          for (int valIdx2 = 0; valIdx2 < valSize2; valIdx2++) {
            final int valIdx = this.indexOfValue(defIdx, other.getValue(defIdx2, valIdx2));
            if (valIdx == -1) {
              // val not found
              return false;
            }
            this.getValueWithAttributes(defIdx, valIdx);
            other.getValueWithAttributes(defIdx2, valIdx2);
            if (this.bb.hasRemaining() && other.bb.hasRemaining() && ArrayHelper.equalsP(this.bb, other.bb)) {
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

  public final int indexOfValue(final int defIdx, final ByteBuffer valBB) {
    for (int valIdx = 0; valIdx < this.size; valIdx++) {
      this.getValue(defIdx, valIdx);
      if (this.bb.hasRemaining() && ArrayHelper.equalsP(this.bb, valBB)) {
        return valIdx;
      }
    }
    this.bb.limit(0);
    return -1;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final DictByteBufferRow other = (DictByteBufferRow) obj;
    return this.equals(other);
  }

  @Override
  protected void finalize() throws Throwable {
    if (this.copied) {
      ArrayHelper.giveBack(this.bb);
    }
    super.finalize();
  }

  public final ByteBuffer getFirstValueAttribute(final int defIdx, final int attrIdx) {
    return this.getAttribute(defIdx, 0, attrIdx);
  }

  public final ByteBuffer getAttribute(final int defIdx, final int valIdx, final int attrIdx) {
    if (defIdx < this.size) {
      if (!this.attrsAnalyzed) {
        this.parseAttributes();
      }
      final int asSize = this.getAttributesSize(defIdx, valIdx);
      if (valIdx < asSize) {
        // System.out.println("defIdx: " + defIdx + ", valIdx: " + valIdx + ", attrIdx: " + attrIdx + "; size: "
        // + size + ", asSize: " + asSize);
        final IntList aStartList = this.attrStartIdx.get(defIdx).get(valIdx);
        final IntList aStopList = this.attrStopIdx.get(defIdx).get(valIdx);

        final int aStartIdx = aStartList.get(attrIdx);
        final int aStopIdx = aStopList.get(attrIdx);
        this.bb.limit(aStopIdx);
        this.bb.position(aStartIdx);
      } else {
        this.bb.limit(0);
      }
    } else {
      this.bb.limit(0);
    }
    return this.bb;
  }

  public final ByteBuffer getFirstValueAttributes(final int defIdx) {
    return this.getAttributes(defIdx, 0);
  }

  public final ByteBuffer getAttributes(final int defIdx, final int valIdx) {
    if (defIdx < this.size) {
      this.bb.limit(this.attrsStopIdx.get(defIdx).get(valIdx));
      this.bb.position(this.attrsStartIdx.get(defIdx).get(valIdx));
    } else {
      this.bb.limit(0);
    }
    return this.bb;
  }

  public final int getFirstValueAttributesSize(final int defIdx) {
    return this.getAttributesSize(defIdx, 0);
  }

  public final int getAttributesSize(final int defIdx, final int valIdx) {
    if (defIdx < this.size) {
      if (!this.attrsAnalyzed) {
        this.parseAttributes();
      }
      return this.attrsSize.get(defIdx).get(valIdx);
    } else {
      return 0;
    }
  }

  /**
   * 
   * @return internal bytebuffer (modified by last request)
   */
  public final ByteBuffer lastResult() {
    return this.bb;
  }

  /**
   * 
   * @return bytebuffer of whole row (may smaller than original)
   */
  public final ByteBuffer getByteBuffer() {
    this.bb.limit(this.actualLimit).position(this.actualPosition);
    return this.bb;
  }

  /**
   * 
   * @return original unmodified byte buffer
   */
  public final ByteBuffer byteBuffer() {
    this.bb.limit(this.originalLimit).position(this.originalPosition);
    return this.bb;
  }

  public final ByteBuffer getDefinitionValuesTo(final int defIdx, final int valIdx) {
    if (defIdx < this.size) {
      final int end = Math.max(this.valueStopIdx.get(defIdx).get(valIdx), this.attrsStopIdx.get(defIdx).get(valIdx));
      this.bb.limit(end);
      this.bb.position(this.defStartIdx.get(defIdx));
    } else {
      this.bb.limit(0);
    }
    return this.bb;
  }

  public final ByteBuffer getDefinitionWithAttributes(final int i) {
    if (i < this.size) {
      this.bb.limit(this.defStopIdx.get(i));
      this.bb.position(this.defStartIdx.get(i));
    } else {
      this.bb.limit(0);
    }
    return this.bb;
  }

  /**
   * 
   * @param lngBB
   * 
   * @return definition index with the given lng, -1 if nothing found
   */
  public final int indexOfLanguage(final ByteBuffer lngBB) {
    for (int i = 0; i < this.size; i++) {
      this.getLanguage(i);
      if (this.bb.hasRemaining() && ArrayHelper.equalsP(this.bb, lngBB)) {
        return i;
      }
    }
    this.bb.limit(0);
    return -1;
  }

  public final ByteBuffer getLanguage(final int defIdx) {
    if (defIdx < this.size) {
      this.bb.limit(this.lngStopIdx.get(defIdx));
      this.bb.position(this.lngStartIdx.get(defIdx));
    } else {
      this.bb.limit(0);
    }
    return this.bb;
  }

  public final ByteBuffer getFirstValue(final int defIdx) {
    return this.getValue(defIdx, 0);
  }

  public final int getValueSize(final int defIdx) {
    if (defIdx < this.size) {
      return this.valuesSize.get(defIdx);
    }
    return 0;
  }

  public final ByteBuffer getValue(final int defIdx, final int valIdx) {
    if (defIdx < this.size) {
      // System.out.println("start: " + valueStartIdx.get(i) + ", stop:" +
      // valueStopIdx.get(i));
      this.bb.limit(this.valueStopIdx.get(defIdx).get(valIdx));
      this.bb.position(this.valueStartIdx.get(defIdx).get(valIdx));
    } else {
      this.bb.limit(0);
    }
    return this.bb;
  }

  public final ByteBuffer getFirstValueWithAttributes(final int defIdx) {
    return this.getValueWithAttributes(defIdx, 0);
  }

  public final ByteBuffer getValueWithAttributes(final int defIdx, final int valIdx) {
    if (defIdx < this.size) {
      this.bb.limit(Math.max(this.valueStopIdx.get(defIdx).get(valIdx), this.attrsStopIdx.get(defIdx).get(valIdx)));
      this.bb.position(this.valueStartIdx.get(defIdx).get(valIdx));
    } else {
      this.bb.limit(0);
    }
    return this.bb;
  }

  public final boolean hasFirstValueAttribute(final int defIdx, final ByteBuffer attrBB) {
    return this.hasAttribute(defIdx, 0, attrBB);
  }

  public final boolean hasAttribute(final int defIdx, final int valIdx, final ByteBuffer attrBB) {
    if (defIdx < this.size) {
      final int asSize = this.getAttributesSize(defIdx, valIdx);
      for (int attrIdx = 0; attrIdx < asSize; attrIdx++) {
        this.getAttribute(defIdx, valIdx, attrIdx);
        if (this.bb.hasRemaining() && ArrayHelper.equalsP(this.bb, attrBB)) {
          return true;
        }
      }
    }
    return false;
  }

  public final boolean hasFirstValueAttributes(final int defIdx) {
    return this.hasAttributes(defIdx, 0);
  }

  public final boolean hasAttributes(final int defIdx, final int valIdx) {
    if (defIdx < this.size) {
      if (!this.attrsAnalyzed) {
        this.parseAttributes();
      }
      return this.attrsSize.get(defIdx).get(valIdx) > 0;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Arrays.hashCode(this.array);
    result = (prime * result) + this.size;
    return result;
  }

  public final boolean isEmpty() {
    return this.size <= 0;
  }

  public boolean isLinkedBy(final DictByteBufferRow other) {
    if (!this.isEmpty() && !other.isEmpty()) {
      int defIdx;
      for (int defIdx2 = 0; defIdx2 < other.size; defIdx2++) {
        defIdx = this.indexOfLanguage(other.getLanguage(defIdx2));
        if (defIdx != -1) {
          final int valSize = this.getValueSize(defIdx);
          final int valSize2 = other.getValueSize(defIdx2);
          for (int valIdx = 0; valIdx < valSize; valIdx++) {
            for (int valIdx2 = 0; valIdx2 < valSize2; valIdx2++) {
              if (ArrayHelper.equalsP(this.getValue(defIdx, valIdx), other.getValue(defIdx2, valIdx2))) {
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
    this.actualPosition = DictHelper.getNextStartPoint(this.array, this.originalPosition, this.originalLimit);
    this.actualLimit = DictHelper.getNextStopPoint(this.array, this.actualPosition, this.originalLimit, DictHelper.ORDER_PARTS);
    this.bb.position(this.actualPosition);
    this.bb.limit(this.actualLimit);

    if (ArrayHelper.sizeP(this.bb) > Helper.SEP_DEFINITION_BYTES.length) {
      this.size = ArrayHelper.countP(this.bb, Helper.SEP_LIST_BYTES) + 1;
    } else {
      this.size = 0;
    }
    this.defStartIdx.size(this.size);
    this.defStopIdx.size(this.size);
    this.lngStartIdx.size(this.size);
    this.lngStopIdx.size(this.size);
    this.valuesSize.size(this.size);
    DictByteBufferRow.ensureCapacity(this.valueStartIdx, this.size);
    DictByteBufferRow.ensureCapacity(this.valueStopIdx, this.size);
    DictByteBufferRow.ensureCapacity(this.attrsSize, this.size);
    DictByteBufferRow.ensureCapacity(this.attrsStartIdx, this.size);
    DictByteBufferRow.ensureCapacity(this.attrsStopIdx, this.size);

    if (this.size > 0) {
      final int start = this.bb.position();
      final int end = this.bb.limit();

      int defIdx = 0;
      int valIdx = 0;
      this.defStartIdx.set(defIdx, start);
      this.lngStartIdx.set(defIdx, start);

      byte b;
      int sepDefIdx = 0;
      int sepListIdx = 0;
      int sepAttrIdx = 0;
      int sepWordIdx = 0;
      boolean sepDefFound = false;
      boolean sepAttrFound = false;
      for (int i = start; i < end; i++) {
        b = this.array[i];
        if (!sepDefFound) {
          if (b == Helper.SEP_DEFINITION_BYTES[sepDefIdx]) {
            if (++sepDefIdx >= Helper.SEP_DEFINITION_BYTES.length) {
              // value start
              this.lngStopIdx.set(defIdx, (i - Helper.SEP_DEFINITION_BYTES.length) + 1);
              this.valueStartIdx.get(defIdx).set(valIdx, i + 1);
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
                this.valueStopIdx.get(defIdx).set(valIdx, (i - Helper.SEP_ATTRS_BYTES.length) + 1);
                this.attrsStartIdx.get(defIdx).set(valIdx, i + 1);
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
              final int stopIdx = (i - Helper.SEP_WORDS_BYTES.length) + 1;
              this.valueStopIdx.get(defIdx).set(valIdx, stopIdx);
              if (sepAttrFound) {
                this.attrsStopIdx.get(defIdx).set(valIdx, stopIdx);
              } else {
                // no attributes
                this.attrsStartIdx.get(defIdx).set(valIdx, 0);
                this.attrsStopIdx.get(defIdx).set(valIdx, 0);
              }
              valIdx++;
              final int nextStartIdx = i + 1;
              this.valueStartIdx.get(defIdx).set(valIdx, nextStartIdx);
              sepAttrFound = false;
              sepWordIdx = 0;
              sepAttrIdx = 0;
            }
          } else {
            sepWordIdx = 0;
          }
          if (b == Helper.SEP_LIST_BYTES[sepListIdx]) {
            if (++sepListIdx >= Helper.SEP_LIST_BYTES.length) {
              final int stopIdx = (i - Helper.SEP_LIST_BYTES.length) + 1;
              // next def start
              this.defStopIdx.set(defIdx, stopIdx);
              if (sepAttrFound) {
                this.attrsStopIdx.get(defIdx).set(valIdx, stopIdx);
              } else {
                this.valueStopIdx.get(defIdx).set(valIdx, stopIdx);
                // no attributes
                this.attrsStartIdx.get(defIdx).set(valIdx, 0);
                this.attrsStopIdx.get(defIdx).set(valIdx, 0);
              }
              this.valuesSize.set(defIdx, valIdx + 1);
              defIdx++;
              final int nextStartIdx = i + 1;
              this.defStartIdx.set(defIdx, nextStartIdx);
              this.lngStartIdx.set(defIdx, nextStartIdx);
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
      this.defStopIdx.set(defIdx, end);
      this.valuesSize.set(defIdx, valIdx + 1);
      if (sepAttrFound) {
        this.attrsStopIdx.get(defIdx).set(valIdx, end);
      } else {
        this.valueStopIdx.get(defIdx).set(valIdx, end);
        this.attrsStartIdx.get(defIdx).set(valIdx, 0);
        this.attrsStopIdx.get(defIdx).set(valIdx, 0);
      }
    }
    this.attrsAnalyzed = false;
    this.valuesSorted = false;
    return this;
  }

  private void parseAttributes() {
    if (!this.attrsAnalyzed) {
      // ensure capacity
      this.attrStartIdx.ensureCapacity(this.size);
      this.attrStopIdx.ensureCapacity(this.size);
      for (int i = this.attrStartIdx.size(); i < this.size; i++) {
        this.attrStartIdx.add(new ArrayList<IntList>(DictByteBufferRow.INITIAL_VALUES_SIZE));
        this.attrStopIdx.add(new ArrayList<IntList>(DictByteBufferRow.INITIAL_VALUES_SIZE));
      }

      for (int defIdx = 0; defIdx < this.size; defIdx++) {
        final int valSize = this.getValueSize(defIdx);
        // ensure capacity
        final ArrayList<IntList> attrStartDefIdx = this.attrStartIdx.get(defIdx);
        final ArrayList<IntList> attrStopDefIdx = this.attrStopIdx.get(defIdx);
        attrStartDefIdx.ensureCapacity(valSize);
        attrStopDefIdx.ensureCapacity(valSize);
        for (int i = attrStartDefIdx.size(); i < valSize; i++) {
          attrStartDefIdx.add(new IntList(DictByteBufferRow.INITIAL_ATTRS_SIZE).size(DictByteBufferRow.INITIAL_ATTRS_SIZE));
          attrStopDefIdx.add(new IntList(DictByteBufferRow.INITIAL_ATTRS_SIZE).size(DictByteBufferRow.INITIAL_ATTRS_SIZE));
        }

        for (int valIdx = 0; valIdx < valSize; valIdx++) {
          // System.out.println("defIdx: " + defIdx + ", valIdx: " + valIdx + ", valSize: " + valSize
          // + ", attrStart: " + attrStartDefIdx.size() + ", attrStop: " + attrStopDefIdx.size());
          this.getAttributes(defIdx, valIdx);
          final IntList attrStartValIdx = attrStartDefIdx.get(valIdx);
          final IntList attrStopValIdx = attrStopDefIdx.get(valIdx);
          if (this.bb.limit() == 0) {
            this.attrsSize.get(defIdx).set(valIdx, 0);
            attrStartValIdx.clear();
            attrStopValIdx.clear();
          } else {
            final int s = ArrayHelper.countP(this.bb, Helper.SEP_ATTRS_BYTES) + 1;
            this.attrsSize.get(defIdx).set(valIdx, s);
            attrStartValIdx.size(s);
            attrStopValIdx.size(s);

            final int start = this.bb.position();
            final int end = this.bb.limit();

            int attrIdx = 0;
            attrStartValIdx.set(attrIdx, start);
            byte b;
            int sepIdx = 0;
            for (int i = start; i < end; i++) {
              b = this.array[i];
              if (b == Helper.SEP_ATTRS_BYTES[sepIdx]) {
                if (++sepIdx >= Helper.SEP_ATTRS_BYTES.length) {
                  // next attribute start
                  attrStopValIdx.set(attrIdx, (i - Helper.SEP_ATTRS_BYTES.length) + 1);
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

      this.attrsAnalyzed = true;
    }
  }

  public DictByteBufferRow parseFrom(final ByteBuffer rowBB) {
    return this.parseFrom(rowBB, false);
  }

  public DictByteBufferRow parseFrom(final ByteBuffer rowBB, final boolean copy) {
    if (copy) {
      if (this.copied) {
        if (this.bb.capacity() < rowBB.remaining()) {
          ArrayHelper.giveBack(this.bb);
          this.bb = ArrayHelper.borrowByteBuffer(rowBB.remaining());
        }
      } else {
        this.bb = ArrayHelper.borrowByteBuffer(rowBB.remaining());
      }
      ArrayHelper.copyP(rowBB, this.bb);
      this.copied = true;
    } else {
      if (this.copied) {
        ArrayHelper.giveBack(this.bb);
      }
      this.bb = ArrayHelper.wrap(rowBB);
      this.copied = false;
    }
    this.array = this.bb.array();
    this.originalLimit = this.bb.limit();
    this.originalPosition = this.bb.position();
    return this.parse();
  }

  public final int size() {
    return this.size;
  }

  public final int limit() {
    return this.originalLimit;
  }

  public final int position() {
    return this.originalPosition;
  }

  public final byte[] array() {
    return this.array;
  }

  public DictByteBufferRow sortValues() {
    if (!this.valuesSorted) {
      for (int defIdx = 0; defIdx < this.size; defIdx++) {
        final int valSize = this.getValueSize(defIdx);
        // insertion sort
        for (int i = 0; i < valSize; i++) {
          for (int j = i; j > 0; j--) {
            final int startIdx1 = this.valueStartIdx.get(defIdx).get(j - 1);
            final int startIdx2 = this.valueStartIdx.get(defIdx).get(j);
            final int stopIdx1 = this.valueStopIdx.get(defIdx).get(j - 1);
            final int stopIdx2 = this.valueStopIdx.get(defIdx).get(j);
            if (ArrayHelper.isSuccessor(this.bb, startIdx1, stopIdx1 - startIdx1, this.bb, startIdx2, stopIdx2 - startIdx2)) {
              this.swapValue(defIdx, j, j - 1);
            } else {
              break;
            }
          }
        }
      }

      this.valuesSorted = true;
    }
    return this;
  }

  private void swapValue(final int defIdx, final int valIdx1, final int valIdx2) {
    final int start = this.valueStartIdx.get(defIdx).get(valIdx1);
    final int stop = this.valueStopIdx.get(defIdx).get(valIdx1);
    final int asStart = this.attrsStartIdx.get(defIdx).get(valIdx1);
    final int asStop = this.attrsStopIdx.get(defIdx).get(valIdx1);
    final int asSize = this.attrsSize.get(defIdx).get(valIdx1);
    final IntList aStart = this.attrStartIdx.get(defIdx).get(valIdx1);
    final IntList aStop = this.attrStopIdx.get(defIdx).get(valIdx1);

    this.valueStartIdx.get(defIdx).set(valIdx1, this.valueStartIdx.get(defIdx).get(valIdx2));
    this.valueStopIdx.get(defIdx).set(valIdx1, this.valueStopIdx.get(defIdx).get(valIdx2));
    this.attrsStartIdx.get(defIdx).set(valIdx1, this.attrsStartIdx.get(defIdx).get(valIdx2));
    this.attrsStopIdx.get(defIdx).set(valIdx1, this.attrsStopIdx.get(defIdx).get(valIdx2));
    this.attrsSize.get(defIdx).set(valIdx1, this.attrsSize.get(defIdx).get(valIdx2));
    this.attrStartIdx.get(defIdx).set(valIdx1, this.attrStartIdx.get(defIdx).get(valIdx2));
    this.attrStopIdx.get(defIdx).set(valIdx1, this.attrStopIdx.get(defIdx).get(valIdx2));

    this.valueStartIdx.get(defIdx).set(valIdx2, start);
    this.valueStopIdx.get(defIdx).set(valIdx2, stop);
    this.attrsStartIdx.get(defIdx).set(valIdx2, asStart);
    this.attrsStopIdx.get(defIdx).set(valIdx2, asStop);
    this.attrsSize.get(defIdx).set(valIdx2, asSize);
    this.attrStartIdx.get(defIdx).set(valIdx2, aStart);
    this.attrStopIdx.get(defIdx).set(valIdx2, aStop);
  }

  public String toString(final int defIdx) {
    this.getDefinitionWithAttributes(defIdx);
    final ByteBuffer tmpBB = ArrayHelper.borrowByteBuffer(this.bb.remaining() * 2);
    tmpBB.put(this.getLanguage(defIdx));
    tmpBB.put((byte) ' ').put((byte) '=').put((byte) ' ');
    final int valSize = this.getValueSize(defIdx);
    boolean firstVal = true;
    for (int valIdx = 0; valIdx < valSize; valIdx++) {
      if (firstVal) {
        firstVal = false;
      } else {
        tmpBB.put((byte) ',').put((byte) ' ');
      }
      tmpBB.put(this.getValue(defIdx, valIdx));

      final int asSize = this.getAttributesSize(defIdx, valIdx);
      if (asSize > 0) {
        tmpBB.put((byte) ' ').put((byte) '(');
        boolean firstAttr = true;
        for (int attrIdx = 0; attrIdx < asSize; attrIdx++) {
          if (firstAttr) {
            firstAttr = false;
          } else {
            tmpBB.put((byte) ',').put((byte) ' ');
          }
          tmpBB.put(this.getAttribute(defIdx, valIdx, attrIdx));
        }
        tmpBB.put((byte) ')');
      }

    }
    tmpBB.limit(tmpBB.position()).rewind();
    final String result = ArrayHelper.toStringP(tmpBB);
    ArrayHelper.giveBack(tmpBB);
    return result;
  }

  public void writeDefinition(final OutputStream out, final int defIdx) throws IOException {
    if ((defIdx >= 0) && (defIdx < this.size)) {
      ArrayHelper.writeP(out, this.getLanguage(defIdx));
      out.write(Helper.SEP_DEFINITION_BYTES);

      final int valSize = this.getValueSize(defIdx);
      boolean firstVal = true;
      for (int valIdx = 0; valIdx < valSize; valIdx++) {
        if (firstVal) {
          firstVal = false;
        } else {
          out.write(Helper.SEP_WORDS_BYTES);
        }
        ArrayHelper.writeP(out, this.getValueWithAttributes(defIdx, valIdx));
      }
    }
  }

  public void write(final OutputStream out, final int firstDefIdx) throws IOException {
    this.writeDefinition(out, firstDefIdx);

    for (int defIdx = 0; defIdx < this.size; defIdx++) {
      if (defIdx != firstDefIdx) {
        out.write(Helper.SEP_LIST_BYTES);
        this.writeDefinition(out, defIdx);
      }
    }
  }

  public final ByteBuffer getFirstAttributeValue(final int defIdx, final int valIdx, final byte[] typeIdBytes) {
    final int asSize = this.getAttributesSize(defIdx, valIdx);
    ByteBuffer aBB;
    for (int attrIdx = 0; attrIdx < asSize; attrIdx++) {
      aBB = this.getAttribute(defIdx, valIdx, attrIdx);
      if (aBB.remaining() > typeIdBytes.length) {
        if (ArrayHelper.equalsP(aBB, typeIdBytes)) {
          aBB.position(aBB.position() + typeIdBytes.length);
          return aBB;

        }
      }
    }
    return (ByteBuffer) this.bb.limit(0);
  }

  public final ByteBuffer[] getAttributeValues(final int defIdx, final int valIdx, final byte[] typeIdBytes) {
    final List<ByteBuffer> aBBs = new LinkedList<>();
    final int asSize = this.getAttributesSize(defIdx, valIdx);
    ByteBuffer aBB;
    for (int attrIdx = 0; attrIdx < asSize; attrIdx++) {
      aBB = this.getAttribute(defIdx, valIdx, attrIdx);
      if (aBB.remaining() > typeIdBytes.length) {
        if (ArrayHelper.equalsP(aBB, typeIdBytes)) {
          aBB.position(aBB.position() + typeIdBytes.length);
          aBBs.add(ArrayHelper.wrap(aBB));
        }
      }
    }
    return aBBs.toArray(new ByteBuffer[aBBs.size()]);
  }

  public ByteBuffer getDefinition(int defIdx) {
    if (defIdx < this.size) {
      this.bb.position(this.defStartIdx.get(defIdx));
      this.bb.limit(this.defStopIdx.get(defIdx));
    } else {
      this.bb.limit(0);
    }
    return this.bb;
  }
}
