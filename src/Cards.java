import java.util.*;

public final class Cards {

  public static List<Card> getAll() {
    final List<Card> cards = new ArrayList<>();
    for (final Suit suit : Suit.values()) {
      for (final Rank rank : Rank.values()) {
        cards.add(new Card(suit, rank));
      }
    }
    return cards;
  }

  public static void shuffle(final List<Card> cards) {
    final int ITERATIONS = 1000;
    for (int i = 0; i < ITERATIONS; i++) {
      final int randomIdx = Helpers.randomInRange(0, cards.size() - 1);
      final Card card = cards.remove(randomIdx);
      cards.add(card);
    }
  }

  public static Card[] deal(final List<Card> cards, final int cardsCount) {
    final Card[] dealtCards = new Card[cardsCount];
    for (int i = 0; i < cardsCount; i++) {
      dealtCards[i] = cards.remove(i);
    }
    return dealtCards;
  }
}
