public enum Suit {
  CLUBS(1),
  DIAMONDS(2),
  SPADES(3),
  HEARTS(4);
  private int value;
  private Suit(final int suitValue) {
    this.value = suitValue;
  }
  public int getValue() {
    return this.value;
  }
}
