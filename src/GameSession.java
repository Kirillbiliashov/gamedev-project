import java.util.*;

import enums.Combination;

public final class GameSession {
  private static final int PLAYERS_SEATED = 6;
  public static final int MIN_BALANCE = 5000;
  public static final int MAX_BALANCE = 25000;
  public static final int BB_SIZE = 100;
  public static final int SB_SIZE = 50;
  private List<Card> cards;
  private static List<Card> tableCards;
  private final static Player[] players = new Player[PLAYERS_SEATED];
  private int pot;
  private int bbIdx;
  private int currRaiseSum;
  private int playersPlayed;
  private boolean isPreflop = true;
  private int handsPlayed;
  private final String[] STAGES = { "Flop", "Turn", "River" };

  public void start(final int yourBalance, final String nickname) {
    if (handsPlayed == 0) {
      for (int i = 0; i < PLAYERS_SEATED; i++) {
        final boolean isUser = i == 0;
        final int playerBalance = Helpers.randomInRange(MIN_BALANCE, MAX_BALANCE);
        final Player player = new Player(isUser ? yourBalance : playerBalance, isUser ? nickname : "Player " + (i + 1));
        players[i] = player;
      }
    }
    newRound();
  }

  private class InfoLogger {
    public static void presentTableCards() {
      final int size = tableCards.size();
      for (int i = 0; i < size; i++) {
        System.out.print(tableCards.get(i).toString() + (i == size - 1 ? "." : ", "));
      }
      System.out.println();
    }

    public static void printCombination() {
      final Player user = players[0];
      final Combination combination = user.getCombination();
      System.out.println("Your combination is " + combination.toString());
    }

    public static void presentCombinations() {
      final String OLD_SYMBOL = "_";
      final String NEW_SYMBOL = " ";
      for (final Player player : players) {
        if (!player.didFold()) {
          final String combination = Helpers.replaceSymbol(player.getCombination().toString(), OLD_SYMBOL, NEW_SYMBOL);
          System.out.println(player.getNickname() + " has got " + handDescription(player.getHand()) +
              " (" + combination.toLowerCase() + ")");
        }
      }
    }

  }

  private void assignPositions() {
    final int sbIdx = handsPlayed == 1 ? Helpers.randomInRange(0, PLAYERS_SEATED - 1) : bbIdx;
    bbIdx = sbIdx == PLAYERS_SEATED - 1 ? 0 : sbIdx + 1;
    final Player sbPlayer = players[sbIdx];
    sbPlayer.setSB();
    sbPlayer.changeBalance(-SB_SIZE);
    final Player bbPlayer = players[bbIdx];
    bbPlayer.setBB();
    bbPlayer.changeBalance(-BB_SIZE);
    pot += SB_SIZE;
    pot += BB_SIZE;
  }

  private void performBettingRound() {
    final int MIN_RAISE_NUMBER = isPreflop ? 8 : 13;
    final int MIN_CALL_NUMBER = isPreflop ? 4 : 7;
    int currIdx = bbIdx == PLAYERS_SEATED - 1 ? 0 : bbIdx + 1;
    while (playersPlayed < PLAYERS_SEATED) {
      final Player player = players[currIdx];
      final boolean isBB = player.isBB();
      if (!player.didFold()) {
        if (player.getBalance() == 0) System.out.println("Player " + player.getNickname() + " went all in");
        else {
          final int handStrength = player.getCombination().ordinal();
          final int randomDecisionNum = Helpers.randomInRange(handStrength, 10 + handStrength);
          if (currIdx == 0) makeUserTurn(player);
          else if (isPreflop ? isBB && currRaiseSum == 0 : currRaiseSum == 0) {
            if (randomDecisionNum >= MIN_RAISE_NUMBER) {
              currRaiseSum = player.raise(currRaiseSum);
              handleRaiseAction();
            } else System.out.println("Player " + player.getNickname() + (isBB ? " (big blind) " : " ")
                  + "checked, balance: " + player.getBalance());
          } else {
            if (randomDecisionNum < MIN_CALL_NUMBER) player.fold();
            else if (randomDecisionNum < MIN_RAISE_NUMBER || player.getBalance() < currRaiseSum) pot += player.call(currRaiseSum);
            else {
              currRaiseSum = player.raise(currRaiseSum);
              handleRaiseAction();
            }
          }
        }
      } else System.out.println("Player " + player.getNickname() + (player.getBalance() == 0 ? " sit out" : " folded"));
      if (++currIdx == PLAYERS_SEATED) currIdx = 0;
      playersPlayed++;
    }
    playersPlayed = 0;
    if (isPreflop) isPreflop = false;
    currRaiseSum = 0;
  }

  private void makeUserTurn(final Player player) {
    final int balance = player.getBalance();
    final Scanner input = new Scanner(System.in);
    final boolean userCanCheck = isPreflop ? bbIdx == 0 && currRaiseSum == 0 : currRaiseSum == 0;
    final boolean userCanRaise = balance > currRaiseSum;
    char action;
    try {
      System.out.print("Your balance is " + balance + ". Enter " + (userCanRaise ? "R to raise, " : "") + "C to "
          + (userCanCheck ? "check: " : "call, F to fold: "));
      action = input.nextLine().charAt(0);
    } catch (Exception e) {
      action = 'C';
    }
    if (userCanCheck && action == 'C')
      System.out.println("Player " + player.getNickname() + " checked, balance: " + balance);
    else {
      if (action == 'F') player.fold();
      else if (action == 'C') pot += player.call(currRaiseSum);
      else if (action == 'R') {
        if (!userCanRaise) pot += player.call(currRaiseSum);
        else {
          int raiseSum = 0;
          while (raiseSum <= BB_SIZE || raiseSum < currRaiseSum) {
            System.out.print("Enter raise sum: ");
            raiseSum = input.nextInt();
          }
          raiseSum = Math.min(player.getBalance(), raiseSum);
          currRaiseSum = player.raiseFixedSum(raiseSum);
          handleRaiseAction();
        }
      }
    }
  }

  private void handleRaiseAction() {
    pot += currRaiseSum;
    playersPlayed = 0;
  }

  private void assignCombinations() {
    for (final Player player : players) {
      final List<Card> playerHand = new ArrayList<>(player.getHand());
      playerHand.addAll(tableCards);
      if (Cards.isRoyalFlush(playerHand)) {
        player.setCombination(Combination.ROYAL_FLUSH);
      } else if (Cards.isStraightFlush(playerHand)) {
        player.setCombination(Combination.STRAIGHT_FLUSH);
      } else if (Cards.isFourOfKind(playerHand)) {
        player.setCombination(Combination.FOUR_OF_A_KIND);
      } else if (Cards.isFullHouse(playerHand)) {
        player.setCombination(Combination.FULL_HOUSE);
      } else if (Cards.isFlush(playerHand)) {
        player.setCombination(Combination.FLUSH);
      } else if (Cards.isStraight(playerHand)) {
        player.setCombination(Combination.STRAIGHT);
      } else if (Cards.isThreeOfKind(playerHand)) {
        player.setCombination(Combination.THREE_OF_A_KIND);
      } else if (Cards.isTwoPairs(playerHand)) {
        player.setCombination(Combination.TWO_PAIRS);
      } else if (Cards.isPair(playerHand)) {
        player.setCombination(Combination.PAIR);
      }
    }
  }

  private static String handDescription(final List<Card> cards) {
    return cards.get(0).toString() + " and " + cards.get(1);
  }

  private void decideWinner() {
    final int strongestHand = Arrays.asList(players)
        .stream()
        .filter(player -> !player.didFold())
        .mapToInt(player -> player.getCombination().ordinal())
        .sorted()
        .reduce(0, Math::max);
    final List<Player> winnersList = Arrays.asList(players)
        .stream()
        .filter(player -> !player.didFold() && player.getCombination().ordinal() == strongestHand)
        .toList();
    final int winnersCount = winnersList.size();
    final int winSum = pot / winnersCount;
    for (final Player winner : winnersList) {
      winner.changeBalance(winSum);
      System.out.println(winner.getNickname() + " won " + winSum + ", new balance: " + winner.getBalance());
    }
  }

  private void resetPrevRoundData() {
    for (final Player player : players) player.resetPrevRoundData();
  }

  private void dealHands() {
    for (int i = 0; i < PLAYERS_SEATED; i++) {
      boolean isUser = i == 0;
      players[i].dealHand(cards);
      if (isUser) {
        final List<Card> hand = players[i].getHand();
        System.out.println(players[i].getNickname() + ", your hand is " + handDescription(hand));
      }
    }
  }

  private void endRound() {
    final Scanner input = new Scanner(System.in);
    System.out.println("Hands played: " + handsPlayed);
    char symbol;
    try {
      System.out.print("Enter Q if you want to quit the game or any other symbol otherwise: ");
      symbol = input.nextLine().charAt(0);
    } catch (Exception e) {
      symbol = ' ';
    }
    if (symbol == 'Q') {
      System.out.println("Thank you for playing");
      return;
    }
    pot = 0;
    isPreflop = true;
    currRaiseSum = 0;
    for (final Player player : players) player.setFolded(player.getBalance() == 0);
    newRound();
  }

  private void newRound() {
    if (players[0].getBalance() == 0) {
      System.out.println("Your balance is 0. Game Over!");
      return;
    }
    handsPlayed++;
    cards = Cards.getAll();
    tableCards = new ArrayList<>();
    Cards.shuffle(cards);
    dealHands();
    assignPositions();
    performBettingRound();
    for (int i = 0; i < STAGES.length; i++) {
      System.out.println("Pot is " + pot);
      Helpers.transport(cards, tableCards, i == 0 ? 3 : 1);
      assignCombinations();
      System.out.print(STAGES[i] + ": ");
      InfoLogger.presentTableCards();
      InfoLogger.printCombination();
      performBettingRound();
      resetPrevRoundData();
    }
    InfoLogger.presentCombinations();
    decideWinner();
    endRound();
  }
}
