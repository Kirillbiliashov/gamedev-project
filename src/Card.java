public final class Card {
  private final Suit suit;
  private final Rank rank;

  public Card(final Suit suit, final Rank rank) {
    this.suit = suit;
    this.rank = rank;
  }

  @Override
  public String toString() {
    return rank.toString().toLowerCase() + " of " + suit.toString().toLowerCase();
  }
}
