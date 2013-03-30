package cn.kk.dict2go;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractListModel;

public class SortedListModel<T> extends AbstractListModel<T> {
  private static final long  serialVersionUID = 971124176779069855L;
  private final SortedSet<T> model;

  public SortedListModel() {
    this.model = new TreeSet<>();
  }

  @Override
  public int getSize() {
    return this.model.size();
  }

  @Override
  public T getElementAt(final int index) {
    final Iterator<T> it = this.model.iterator();
    for (int i = 0;; i++) {
      final T e = it.next();
      if (i == index) {
        return e;
      }
    }
  }

  public void add(final T element) {
    if (this.model.add(element)) {
      this.fireContentsChanged(this, 0, this.getSize());
    }
  }

  public void addAll(final List<T> values) {
    this.model.addAll(values);
    this.fireContentsChanged(this, 0, this.getSize());
  }

  public void clear() {
    this.model.clear();
    this.fireContentsChanged(this, 0, this.getSize());
  }

  public boolean contains(final Object element) {
    return this.model.contains(element);
  }

  public boolean removeElement(final Object element) {
    final boolean removed = this.model.remove(element);
    if (removed) {
      this.fireContentsChanged(this, 0, this.getSize());
    }
    return removed;
  }

  public void removeAll(final List<T> values) {
    this.model.removeAll(values);
    this.fireContentsChanged(this, 0, this.getSize());
  }
}
