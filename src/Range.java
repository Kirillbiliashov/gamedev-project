public final class Range {
  private int start;
  private int end;

  public Range(final int start, final int end) {
    this.start = start;
    this.end = end;
  }

  public boolean contains(final int num) {
    return this.start <= num && num <= this.end;
  }
}
