package src.app;
import src.enums.*;

public final class Card {
  private final Suit suit;
  private final Rank rank;

  public Card(final Suit suit, final Rank rank) {
    this.suit = suit;
    this.rank = rank;
  }

  public Rank getRank() {
    return this.rank;
  }

  public Suit getSuit() {
    return this.suit;
  }

  @Override
  public String toString() {
    return rank.toString().toLowerCase() + " of " + suit.toString().toLowerCase();
  }
}
