package cn.kk.kkdict.types;

public interface KeyType<T> {
  int getId();

  String getKey();

  byte[] getKeyBytes();
}
