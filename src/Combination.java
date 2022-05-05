import java.util.*;

public enum Combination {
  HIGH_CARD {
    @Override
    public boolean check(final List<Card> cards) {
      return true;
    }
  },
  PAIR {
    @Override
    public boolean check(final List<Card> cards) {
      final int EQUAL_RANK_VALUES_REQUIRED = 2;
      final int[] rankValues = getSortedValues(cards, Rank.class);
      return Helpers.hasEqualNumbers(rankValues, EQUAL_RANK_VALUES_REQUIRED);
    }
  },
  TWO_PAIRS {
    @Override
    public boolean check(final List<Card> cards) {
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
  },
  THREE_OF_A_KIND {
    @Override
    public boolean check(final List<Card> cards) {
      final int EQUAL_RANK_VALUES_REQUIRED = 3;
      final int[] rankValues = getSortedValues(cards, Rank.class);
      return Helpers.hasEqualNumbers(rankValues, EQUAL_RANK_VALUES_REQUIRED);
    }
  },
  STRAIGHT {
    @Override
    public boolean check(final List<Card> cards) {
      final int CARDS_REQUIRED_FOR_STRAIGHT = 5;
      final int[] rankValues = getSortedValues(cards, Rank.class);
      final int size = cards.size();
      for (int i = 0; i < size; i++) {
        rankValues[i] -= i;
      }
      return Helpers.hasEqualNumbers(rankValues, CARDS_REQUIRED_FOR_STRAIGHT);
    }
  },
  FLUSH {
    @Override
    public boolean check(final List<Card> cards) {
      final int EQUAL_SUIT_VALUES_REQUIRED = 5;
      final int[] suitValues = getSortedValues(cards, Suit.class);
      return Helpers.hasEqualNumbers(suitValues, EQUAL_SUIT_VALUES_REQUIRED);
    }
  },
  FULL_HOUSE {
    @Override
    public boolean check(final List<Card> cards) {
      final int HIGHER_COMBINATION_CARDS_REQUIRED = 3;
      final int LOWER_COMBINATION_CARDS_REQUIRED = 2;
      final int[] rankValues = getSortedValues(cards, Rank.class);
      final int threeOfKindRankVal;
      for (int i = 0; i < rankValues.length - HIGHER_COMBINATION_CARDS_REQUIRED + 1; i++) {
        final int[] threeOfKindSubArr = Arrays.copyOfRange(rankValues, i, i + 3);
        if (Helpers.hasEqualNumbers(threeOfKindSubArr, 3)) {
          threeOfKindRankVal = rankValues[i];
          for (int m = 0; m < rankValues.length - LOWER_COMBINATION_CARDS_REQUIRED + 1; m++) {
            final int[] pairSubArr = Arrays.copyOfRange(rankValues, m, m + LOWER_COMBINATION_CARDS_REQUIRED);
            if (Helpers.hasEqualNumbers(pairSubArr, 2) && rankValues[m] != threeOfKindRankVal) return true;
          }
          return false;
        }
      }
      return false;
    }
  },
  FOUR_OF_A_KIND {
    @Override
    public boolean check(final List<Card> cards) {
      final int EQUAL_RANK_VALUES_REQUIRED = 4;
      final int[] rankValues = getSortedValues(cards, Rank.class);
      return Helpers.hasEqualNumbers(rankValues, EQUAL_RANK_VALUES_REQUIRED);
    }
  },
  STRAIGHT_FLUSH {
    @Override
    public boolean check(final List<Card> cards) {
      return STRAIGHT.check(cards) && FLUSH.check(cards);
    }
  },
  ROYAL_FLUSH {
    @Override
    public boolean check(final List<Card> cards) {
      final int maxValue = Rank.ACE.ordinal();
      final int lastIdx = cards.size() - 1;
      final boolean isStraightFlush = STRAIGHT.check(cards) && FLUSH.check(cards);
      final int lastCardValue = cards.get(lastIdx).getRank().ordinal();
      return isStraightFlush && lastCardValue == maxValue;
    }
  };

  private static <T> int[] getSortedValues(final List<Card> cards, final Class<T> enumClass) {
    final String rankClassName = Rank.class.getName();
    final String className = enumClass.getName();
    return cards
        .stream()
        .mapToInt(card -> className.equals(rankClassName) ? card.getRank().ordinal() : card.getSuit().ordinal())
        .sorted()
        .toArray();
  }

  public abstract boolean check(final List<Card> cards);
}