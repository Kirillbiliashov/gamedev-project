public enum Action {
  FOLD(new Range(0, 4)),
  CALL(new Range(5, 7)),
  RAISE(new Range(8, 20)),
  CHECK(new Range(20, 30));

  private Range range;

  private Action(final Range range) {
    this.range = range;
  }

  public Range getRange() {
    return this.range;
  }
}