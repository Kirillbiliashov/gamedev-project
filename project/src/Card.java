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
    final int ENUM_IDX_SHIFT = 2;
    final int rankValue = rank.ordinal();
    final String rankLetter = rank.toString().substring(0, 1);
    final String rankNumber = Integer.toString(rankValue + ENUM_IDX_SHIFT);
    final String cardSymbol = rankValue < Rank.TEN.ordinal() ? rankNumber : rankLetter;
    return cardSymbol + suit.getEmoji();
  }
}
