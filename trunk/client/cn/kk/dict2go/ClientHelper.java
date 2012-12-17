package cn.kk.dict2go;

public final class ClientHelper {
  public static final int readInt(final byte[] ba, final int offset) {
    return ((ba[offset] << 24) + (ba[offset + 1] << 16) + (ba[offset + 2] << 8) + (ba[offset + 3] << 0));
  }
}
