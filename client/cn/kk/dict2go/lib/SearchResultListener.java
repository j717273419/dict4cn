package cn.kk.dict2go.lib;

public abstract class SearchResultListener {
  private final SearchType type;

  public SearchResultListener(final SearchType type) {
    this.type = type;
  }

  public SearchType getType() {
    return this.type;
  }

  public abstract void onResultChanged(final SearchTask result);
}
