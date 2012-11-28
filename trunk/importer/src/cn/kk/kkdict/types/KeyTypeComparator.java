package cn.kk.kkdict.types;

import java.util.Comparator;

public class KeyTypeComparator<T> implements Comparator<KeyType<T>> {

  @Override
  public int compare(KeyType<T> o1, KeyType<T> o2) {
    int id1 = o1.getId();
    int id2 = o2.getId();
    if ((id1 != -1) && (id2 != -1)) {
      return id1 - id2;
    } else if ((id1 == -1) && (id2 == -1)) {
      return o1.getKey().compareTo(o2.getKey());
    } else if (id2 == -1) {
      return -1;
    } else {
      return 1;
    }
  }

}
