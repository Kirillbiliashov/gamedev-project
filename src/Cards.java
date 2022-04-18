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

  public static List<Card> deal(final List<Card> cards, final int cardsCount) {
    final List<Card> dealtCards = new ArrayList<>(cardsCount);
    for (int i = 0; i < cardsCount; i++) {
      dealtCards.add(cards.remove(i));
    }
    return dealtCards;
  }

  public static boolean isRoyalFlush(final List<Card> cards) {
    final int MAX_CARD_VALUE = 13;
    final int lastIdx = cards.size() - 1;
    if (isStraight(cards) && isFlush(cards)) {
      final int cardValue = cards.get(lastIdx).getRank().getValue();
      return cardValue == MAX_CARD_VALUE;
    }
    return false;
  }

  public static boolean isStraightFlush(final List<Card> cards) {
    return isStraight(cards) && isFlush(cards);
  }

  public static boolean isFourOfKind(final List<Card> cards) {
    final int EQUAL_RANK_VALUES_REQUIRED = 4;
    final int[] rankValues = getSortedValues(cards, Rank.class);
    return Helpers.hasEqualNumbers(rankValues, EQUAL_RANK_VALUES_REQUIRED);
  }

  public static boolean isFullHouse(final List<Card> cards) {
    final int HIGHER_COMBINATION_CARDS_REQUIRED = 3;
    final int LOWER_COMBINATION_CARDS_REQUIRED = 2;
    final int[] rankValues = getSortedValues(cards, Rank.class);
    final int threeOfKindRankVal;
    for (int i = 0; i < rankValues.length - HIGHER_COMBINATION_CARDS_REQUIRED + 1; i++) {
      int[] threeOfKindSubArr = Arrays.copyOfRange(rankValues, i, i + 3);
      if (Helpers.hasEqualNumbers(threeOfKindSubArr, 3)) {
        threeOfKindRankVal = rankValues[i];
        for (int m = 0; m < rankValues.length - LOWER_COMBINATION_CARDS_REQUIRED + 1; m++) {
          int[] pairSubArr = Arrays.copyOfRange(rankValues, m, m + LOWER_COMBINATION_CARDS_REQUIRED);
          if (Helpers.hasEqualNumbers(pairSubArr, 2) && rankValues[m] != threeOfKindRankVal) return true;
        }
        return false;
      }
    }
    return false;
  }

  public static boolean isFlush(final List<Card> cards) {
    final int EQUAL_SUIT_VALUES_REQUIRED = 5;
    final int[] suitValues = getSortedValues(cards, Suit.class);
    return Helpers.hasEqualNumbers(suitValues, EQUAL_SUIT_VALUES_REQUIRED);
  }

  public static boolean isStraight(final List<Card> cards) {
    final int CARDS_REQUIRED_FOR_STRAIGHT = 5;
    final int[] rankValues = getSortedValues(cards, Rank.class);
    for (int i = 0; i < cards.size(); i++) {
      rankValues[i] -= i;
    }
    return Helpers.hasEqualNumbers(rankValues, CARDS_REQUIRED_FOR_STRAIGHT);
  }

  public static boolean isThreeOfKind(final List<Card> cards) {
    final int EQUAL_RANK_VALUES_REQUIRED = 3;
    final int[] rankValues = getSortedValues(cards, Rank.class);
    return Helpers.hasEqualNumbers(rankValues, EQUAL_RANK_VALUES_REQUIRED);
  }

  public static boolean isTwoPairs(final List<Card> cards) {
    final int EQUAL_RANK_VALUES_REQUIRED = 2;
    final int[] rankValues = getSortedValues(cards, Rank.class);
    final int length = rankValues.length;
    for (int i = 1; i < length - 1; i++) {
      if (rankValues[i] == rankValues[i - 1]) {
        int[] otherValues = Arrays.copyOfRange(rankValues, i + 1, length);
        return Helpers.hasEqualNumbers(otherValues, EQUAL_RANK_VALUES_REQUIRED);
      }
    }
    return false;
  }

  public static boolean isPair(final List<Card> cards) {
    final int EQUAL_RANK_VALUES_REQUIRED = 2;
    final int[] rankValues = getSortedValues(cards, Rank.class);
    return Helpers.hasEqualNumbers(rankValues, EQUAL_RANK_VALUES_REQUIRED);
  }

  private static <T> int[] getSortedValues(final List<Card> cards, final Class<T> enumClass) {
    String rankClassName = Rank.class.getName();
    String className = enumClass.getName();
    return cards
        .stream()
        .mapToInt(card -> className.equals(rankClassName) ? card.getRank().getValue() : card.getSuit().getValue())
        .sorted()
        .toArray();
  }
}
