package src;

import enums.*;

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
    final int rankValue = rank.ordinal();
    return (rankValue < Rank.TEN.ordinal() ? rankValue + 2 : rank.toString().substring(0, 1)) + suit.getEmoji();
  }
}
