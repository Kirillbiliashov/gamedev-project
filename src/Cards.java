import java.util.*;

public class Cards {

  public static List<Card> getAll() {
    List<Card> cards = new ArrayList<>();
    for (Suit suit : Suit.values()) {
      for (Rank rank : Rank.values()) {
        cards.add(new Card(suit, rank));
      }
    }
    return cards;
  }

  public static void shuffle(List<Card> cards) {
    final int ITERATIONS = 1000;
    for (int i = 0; i < ITERATIONS; i++) {
      int randomIdx = Helpers.randomInRange(0, cards.size() - 1);
      Card card = cards.remove(randomIdx);
      cards.add(card);
    }
  }

  public static Card[] deal(List<Card> cards, int cardsCount) {
    Card[] dealtCards = new Card[cardsCount];
    for (int i = 0; i < cardsCount; i++) {
      Card card = cards.remove(i);
      dealtCards[i] = card;
    }
    return dealtCards;
  }
}
