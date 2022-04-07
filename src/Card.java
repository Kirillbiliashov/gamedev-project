public class Card {
  private Suit suit;
  private Rank rank;

  public Card(Suit suit, Rank rank) {
    this.suit = suit;
    this.rank = rank;
  }

  public String toString() {
    return rank.toString().toLowerCase() + " of " + suit.toString().toLowerCase();
  }
}
