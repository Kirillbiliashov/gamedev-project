package src;

import java.util.*;

import enums.Combination;
import staticClasses.*;

public final class GameSession {
  public static final int PLAYERS_SEATED = 6;
  public static final int MIN_BALANCE = 5000;
  public static final int MAX_BALANCE = 25000;
  public static final int BIG_BLIND_SIZE = 100;
  public static final int SMALL_BLIND_SIZE = 50;
  public static final int MAX_FOLD_NUM = 4;
  public static final int MAX_CALL_NUM = 7;
  public static final int MAX_RAISE_NUM = 20;
  public static final int MAX_CHECK_NUM = 30;
  private List<Card> deck;
  private static List<Card> tableCards;
  private final static Player[] players = new Player[PLAYERS_SEATED];
  private int handsPlayed;
  private static final String[] ROUNDS = { "Flop", "Turn", "River" };
  public static final int ROUNDS_LENGTH = ROUNDS.length;
  private final RoundHandler roundHandler = new RoundHandler(players);
  private final WinnersHandler winnersHandler = new WinnersHandler(players);

  public void start(final int yourBalance, final String nickname) {
    if (this.handsPlayed == 0) {
      final int BALANCE_ACCURACY = 5;
      for (int i = 0; i < PLAYERS_SEATED; i++) {
        final boolean isUser = i == 0;
        final int playerBalance = Helpers.randomInRange(MIN_BALANCE, MAX_BALANCE,
            BALANCE_ACCURACY);
        final int balance = isUser ? yourBalance : playerBalance;
        final String name = isUser ? nickname : "Player " + (i + 1);
        players[i] = new Player(balance, name);
      }
    }
    this.newGame();
  }

  private class InfoLogger {
    private final static String OLD_SYMBOL = "_";
    private final static String NEW_SYMBOL = " ";

    public static void presentTableCards() {
      for (final Card card : tableCards) {
        System.out.print(card.toString() + " ");
      }
      System.out.println();
    }

    private static void printCombination(final Combination combination) {
      final String combinationStr = Helpers.replaceSymbol(combination.toString(),
          OLD_SYMBOL, NEW_SYMBOL);
      System.out.println("(" + combinationStr.toLowerCase() + ")");
    }

    private static void presentCombinations() {
      for (final Player player : players) {
        if (player.isActive()) {
          System.out.print(player.getNickname() + " has got ");
          printHandDescription(player);
        }
      }
    }

    private static void printHandDescription(final Player player) {
      final List<Card> hand = player.getHand();
      final String firstHandStr = hand.get(0).toString();
      final String secondHandStr = hand.get(1).toString();
      System.out.print(firstHandStr + " and " + secondHandStr);
      printCombination(player.getCombination());
    }

    private static void printUserCombination() {
      System.out.print(players[0].getNickname() + ", your hand is ");
      InfoLogger.printHandDescription(players[0]);
    }
  }

  private void assignCombinations() {
    List<Combination> combinationsList = Arrays.asList(Combination.values());
    Collections.reverse(combinationsList);
    for (final Player player : players) {
      if (player.isActive()) {
        final List<Card> playerHand = new ArrayList<>(player.getHand());
        playerHand.addAll(tableCards);
        for (final Combination combination : combinationsList) {
          if (combination.check(playerHand)) {
            player.setCombination(combination);
            break;
          }
        }
      }
    }
  }

  private void resetGameData() {
    for (final Player player : players) player.resetGameData();
    this.roundHandler.resetPotSize();
  }

  private void dealHands() {
    final int CARDS_TO_DEAL = 2;
    for (int i = 0; i < PLAYERS_SEATED; i++) {
      players[i].setHand(Cards.deal(this.deck, CARDS_TO_DEAL));
    }
  }

  private void endGame() {
    this.resetGameData();
    final Scanner input = new Scanner(System.in);
    System.out.println("Hands played: " + this.handsPlayed);
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
    this.newGame();
  }

  private void handOutCards() {
    this.deck = Cards.getAll();
    tableCards = new ArrayList<>();
    Cards.shuffle(this.deck);
    this.dealHands();
  }

  private void newGame() {
    if (players[0].getBalance() == 0) {
      System.out.println("Your balance is 0. Game Over!");
      return;
    }
    this.handsPlayed++;
    this.handOutCards();
    this.assignCombinations();
    InfoLogger.printUserCombination();
    this.roundHandler.assignPositions(this.handsPlayed);
    this.roundHandler.setPreflop();
    System.out.println("Preflop: ");
    this.roundHandler.handle();
    this.performPostflopRounds();
    InfoLogger.presentCombinations();
    this.winnersHandler.setPotSize(roundHandler.getPot());
    this.winnersHandler.handle();
    this.endGame();
  }

  private void performPostflopRounds() {
    for (int i = 0; i < ROUNDS.length; i++) {
      Helpers.transport(this.deck, tableCards, i == 0 ? 3 : 1);
      this.assignCombinations();
      System.out.print(ROUNDS[i] + ": ");
      InfoLogger.presentTableCards();
      InfoLogger.printUserCombination();
      this.roundHandler.handle();
    }
  }
}
