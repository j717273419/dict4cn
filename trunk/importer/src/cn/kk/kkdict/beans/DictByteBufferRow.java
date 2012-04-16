package cn.kk.kkdict.beans;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * 
 * This class can be reused by calling parseFrom-method. This class is not
 * thread-safe. ByteBuffer result is temporary and will be changed on eath
 * method call.
 * 
 */
public class DictByteBufferRow {

	private static final int INITIAL_ATTRS_SIZE = 20;
	private static final int INITIAL_DEFS_SIZE = 200;

	private final static void ensureCapacity(final ArrayList<IntList> list,
			final int capacity) {
		list.ensureCapacity(capacity);
		for (int i = list.size(); i < capacity; i++) {
			list.add(new IntList(INITIAL_ATTRS_SIZE));
		}
	}

	public static final DictByteBufferRow parse(final ByteBuffer rowBB) {
		return parse(rowBB, false);
	}

	public static final DictByteBufferRow parse(final ByteBuffer rowBB,
			final boolean copy) {
		DictByteBufferRow row = new DictByteBufferRow();
		row.parseFrom(rowBB, copy);
		return row;
	}

	private byte[] array;

	private boolean attrsAnalyzed;
	private IntList attrsSize = new IntList(INITIAL_DEFS_SIZE);
	private IntList attrsStartIdx = new IntList(INITIAL_DEFS_SIZE);

	private IntList attrsStopIdx = new IntList(INITIAL_DEFS_SIZE);
	private ArrayList<IntList> attrStartIdx = new ArrayList<IntList>(
			INITIAL_DEFS_SIZE);
	private ArrayList<IntList> attrStopIdx = new ArrayList<IntList>(
			INITIAL_DEFS_SIZE);
	private ByteBuffer bb;
	private IntList defStartIdx = new IntList(INITIAL_DEFS_SIZE);
	private IntList defStopIdx = new IntList(INITIAL_DEFS_SIZE);
	private IntList lngStartIdx = new IntList(INITIAL_DEFS_SIZE);
	private IntList lngStopIdx = new IntList(INITIAL_DEFS_SIZE);
	private int size;
	private IntList valueStartIdx = new IntList(INITIAL_DEFS_SIZE);
	private IntList valueStopIdx = new IntList(INITIAL_DEFS_SIZE);
	private int actualLimit;
	private int actualPosition;
	private boolean copied;

	public DictByteBufferRow() {
	}

	public void debug(final int i) {
		System.out.println("DictByteBufferRow [size=" + size
				+ ", attrsAnalyzed=" + attrsAnalyzed + ", attrsSize="
				+ attrsSize.get(i) + ", attrsStartIdx=" + attrsStartIdx.get(i)
				+ ", attrsStopIdx=" + attrsStopIdx.get(i) + ", defStartIdx="
				+ defStartIdx.get(i) + ", defStopIdx=" + defStopIdx.get(i)
				+ ", lngStartIdx=" + lngStartIdx.get(i) + ", lngStopIdx="
				+ lngStopIdx.get(i) + ", valueStartIdx=" + valueStartIdx.get(i)
				+ ", valueStopIdx=" + valueStopIdx.get(i) + "]");
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
		int idx;
		for (int i = 0; i < size; i++) {
			idx = indexOfLanguage(other.getLanguage(i));
			if (idx == -1) {
				return false;
			} else {
				getValueWithAttributes(idx);
				other.getValueWithAttributes(i);
				if (bb.hasRemaining() && other.bb.hasRemaining()
						&& ArrayHelper.equalsP(bb, other.bb)) {
					continue;
				} else {
					// System.out.println(ArrayHelper.toStringP(bb) + ", " +
					// ArrayHelper.toStringP(other.bb));
					return false;
				}
			}
		}
		return true;
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
		ArrayHelper.giveBack(bb);
		super.finalize();
	}

	public final ByteBuffer getAttribute(final int i, final int j) {
		if (i < size) {
			if (!attrsAnalyzed) {
				parseAttributes();
			}
			if (j < attrsSize.get(i)) {
				bb.limit(attrStopIdx.get(i).get(j));
				bb.position(attrStartIdx.get(i).get(j));
			} else {
				bb.limit(0);
			}
		} else {
			bb.limit(0);
		}
		return bb;
	}

	public final ByteBuffer getAttributes(final int i) {
		if (i < size) {
			bb.limit(attrsStopIdx.get(i));
			bb.position(attrsStartIdx.get(i));
		} else {
			bb.limit(0);
		}
		return bb;
	}

	public final int getAttributesSize(final int i) {
		if (i < size) {
			if (!attrsAnalyzed) {
				parseAttributes();
			}
			return attrsSize.get(i);
		} else {
			return 0;
		}
	}

	/**
	 * 
	 * @return internal bytebuffer, should not be modified
	 */
	public final ByteBuffer getByteBuffer() {
		bb.limit(actualLimit).position(actualPosition);
		return bb;
	}

	public final ByteBuffer getDefinition(final int i) {
		if (i < size) {
			final int end = Math.max(valueStopIdx.get(i), attrsStopIdx.get(i));
			bb.limit(end);
			bb.position(defStartIdx.get(i));
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
				bb.limit(0);
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

	public final ByteBuffer getValue(final int i) {
		if (i < size) {
			// System.out.println("start: " + valueStartIdx.get(i) + ", stop:" +
			// valueStopIdx.get(i));
			bb.limit(valueStopIdx.get(i));
			bb.position(valueStartIdx.get(i));
		} else {
			bb.limit(0);
		}
		return bb;
	}

	public final ByteBuffer getValueWithAttributes(final int i) {
		if (i < size) {
			bb.limit(defStopIdx.get(i));
			bb.position(valueStartIdx.get(i));
		} else {
			bb.limit(0);
		}
		return bb;
	}

	public final boolean hasAttribute(final int i, ByteBuffer attrBB) {
		if (bb == attrBB) {
			attrBB = ByteBuffer.wrap(ArrayHelper.toBytesP(attrBB));
		}
		if (i < size) {
			final int s = getAttributesSize(i);
			for (int j = 0; j < s; j++) {
				getAttribute(i, j);
				if (bb.hasRemaining() && ArrayHelper.equals(bb, attrBB)) {
					return true;
				}
			}
		}
		return false;
	}

	public final boolean hasAttributes(final int i) {
		if (i < size) {
			if (!attrsAnalyzed) {
				parseAttributes();
			}
			return attrsSize.get(i) > 0;
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
			int idx;
			for (int i = 0; i < other.size; i++) {
				idx = indexOfLanguage(other.getLanguage(i));
				if (idx != -1
						&& ArrayHelper
								.equalsP(getValue(idx), other.getValue(i))) {
					return true;
				}
			}
		}
		return false;
	}

	private void parse() {
		if (ArrayHelper.sizeP(bb) > Helper.SEP_DEFINITION_BYTES.length) {
			size = ArrayHelper.countP(bb, Helper.SEP_LIST_BYTES) + 1;
		} else {
			size = 0;
		}
		defStartIdx.size(size);
		defStopIdx.size(size);
		lngStartIdx.size(size);
		lngStopIdx.size(size);
		valueStartIdx.size(size);
		valueStopIdx.size(size);
		attrsStartIdx.size(size);
		attrsStopIdx.size(size);
		attrsSize.size(size);
		ensureCapacity(attrStartIdx, size);
		ensureCapacity(attrStopIdx, size);
		actualLimit = bb.limit();
		actualPosition = bb.position();

		if (size > 0) {
			final int start = bb.position();
			final int end = bb.limit();

			int idx = 0;
			defStartIdx.set(idx, start);
			lngStartIdx.set(idx, start);

			byte b;
			int sepDefIdx = 0;
			int sepPartIdx = 0;
			int sepAttrIdx = 0;
			boolean sepDefFound = false;
			boolean sepAttrFound = false;
			for (int i = start; i < end; i++) {
				b = array[i];
				if (!sepDefFound) {
					if (b == Helper.SEP_DEFINITION_BYTES[sepDefIdx]) {
						if (++sepDefIdx >= Helper.SEP_DEFINITION_BYTES.length) {
							// value start
							lngStopIdx.set(idx, i
									- Helper.SEP_DEFINITION_BYTES.length + 1);
							valueStartIdx.set(idx, i + 1);
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
								valueStopIdx.set(idx, i
										- Helper.SEP_ATTRS_BYTES.length + 1);
								attrsStartIdx.set(idx, i + 1);
								sepAttrFound = true;
								sepAttrIdx = 0;
							}
						} else {
							sepAttrIdx = 0;
						}
					}
					if (b == Helper.SEP_LIST_BYTES[sepPartIdx]) {
						if (++sepPartIdx >= Helper.SEP_LIST_BYTES.length) {
							final int stopIdx = i
									- Helper.SEP_LIST_BYTES.length + 1;
							// next def start
							defStopIdx.set(idx, stopIdx);
							if (sepAttrFound) {
								attrsStopIdx.set(idx, stopIdx);
							} else {
								valueStopIdx.set(idx, stopIdx);
								attrsStartIdx.set(idx, 0);
								attrsStopIdx.set(idx, 0);
							}
							idx++;
							final int nextStartIdx = i + 1;
							defStartIdx.set(idx, nextStartIdx);
							lngStartIdx.set(idx, nextStartIdx);
							sepDefFound = false;
							sepAttrFound = false;
							sepPartIdx = 0;
							sepAttrIdx = 0;
							sepDefIdx = 0;
						}
					} else {
						sepPartIdx = 0;
					}
				}
			}
			// last end
			defStopIdx.set(idx, end);
			if (sepAttrFound) {
				attrsStopIdx.set(idx, end);
			} else {
				valueStopIdx.set(idx, end);
				attrsStartIdx.set(idx, 0);
				attrsStopIdx.set(idx, 0);
			}
		}
		attrsAnalyzed = false;
	}

	private void parseAttributes() {
		for (int idx = 0; idx < size; idx++) {
			getAttributes(idx);
			if (bb.limit() == 0) {
				attrStartIdx.get(idx).clear();
				attrStopIdx.get(idx).clear();
			} else {
				final int s = ArrayHelper.countP(bb, Helper.SEP_ATTRS_BYTES) + 1;
				attrsSize.set(idx, s);
				attrStartIdx.get(idx).size(s);
				attrStopIdx.get(idx).size(s);

				int start = bb.position();
				int end = bb.limit();

				final IntList currentAttrsStart = attrStartIdx.get(idx);
				final IntList currentAttrsStop = attrStopIdx.get(idx);

				int attrIdx = 0;
				currentAttrsStart.set(attrIdx, start);
				byte b;
				int sepIdx = 0;
				for (int i = start; i < end; i++) {
					b = array[i];
					if (b == Helper.SEP_ATTRS_BYTES[sepIdx]) {
						if (++sepIdx >= Helper.SEP_ATTRS_BYTES.length) {
							// next attribute start
							currentAttrsStop.set(attrIdx, i
									- Helper.SEP_ATTRS_BYTES.length + 1);
							attrIdx++;
							currentAttrsStart.set(attrIdx, i + 1);
							sepIdx = 0;
						}
					} else {
						sepIdx = 0;
					}
				}
				// last attribute
				currentAttrsStop.set(attrIdx, end);
			}
		}
		attrsAnalyzed = true;
	}

	public void parseFrom(final ByteBuffer rowBB) {
		parseFrom(rowBB, false);
	}

	public void parseFrom(final ByteBuffer rowBB, final boolean copy) {
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
		parse();
	}

	public final int size() {
		return size;
	}

	public final int limit() {
		return actualLimit;
	}

	public final int position() {
		return actualPosition;
	}

	public final byte[] array() {
		return array;
	}
}
