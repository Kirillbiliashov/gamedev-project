import java.util.*;

public final class GameSession {
  public static final int PLAYERS_SEATED = 6;
  public static final int MIN_BALANCE = 5000;
  public static final int MAX_BALANCE = 25000;
  public static final int BB_SIZE = 100;
  public static final int SB_SIZE = 50;
  public static final int MAX_FOLD_NUM = 4;
  public static final int MAX_CALL_NUM = 7;
  public static final int MAX_RAISE_NUM = 20;
  public static final int MAX_CHECK_NUM = 30;
  private List<Card> deck;
  private static List<Card> tableCards;
  private final static Player[] players = new Player[PLAYERS_SEATED];
  private int handsPlayed;
  private static final String[] ROUNDS = { "Preflop", "Flop", "Turn", "River" };
  public static final int ROUNDS_LENGTH = ROUNDS.length;
  private final RoundHandler roundHandler = new RoundHandler(players);
  private final WinnersHandler winnersHandler = new WinnersHandler(players);

  public void start(final int yourBalance, final String nickname) {
    if (handsPlayed == 0) {
      for (int i = 0; i < PLAYERS_SEATED; i++) {
        final boolean isUser = i == 0;
        final int playerBalance = Helpers.randomInRange(MIN_BALANCE, MAX_BALANCE);
        players[i] = new Player(isUser ? yourBalance : playerBalance, isUser ? nickname : "Player " + (i + 1));
      }
    }
    newGame();
  }

  private class InfoLogger {
    private final static String OLD_SYMBOL = "_";
    private final static String NEW_SYMBOL = " ";

    public static void presentTableCards() {
      for (final Card card : tableCards) System.out.print(card.toString() + " ");
      System.out.println();
    }

    public static void printCombination() {
      final Player user = players[0];
      final String combinationStr = Helpers.replaceSymbol(user.getCombination().toString(), OLD_SYMBOL, NEW_SYMBOL);
      System.out.println("Your combination is " + combinationStr.toLowerCase());
    }

    public static void presentCombinations() {
      for (final Player player : players) {
        if (player.isActive()) {
          System.out.print(player.getNickname() + " has got ");
          printHandDescription(player);
        }
      }
    }
    private static void printHandDescription(final Player player) {
      final List<Card> hand = player.getHand();
      final String combination = Helpers.replaceSymbol(player.getCombination().toString(), OLD_SYMBOL, NEW_SYMBOL);
      System.out.println(hand.get(0).toString() + " and " + hand.get(1) +  " (" + combination + ")");
    }
  }

  private void assignCombinations() {
    for (final Player player : players) {
      final List<Card> playerHand = new ArrayList<>(player.getHand());
      playerHand.addAll(tableCards);
      List<Combination> combinationsList = Arrays.asList(Combination.values());
      Collections.reverse(combinationsList);
      for (final Combination combination : combinationsList) {
        if (combination.check(playerHand)) {
          player.setCombination(combination);
          break;
        }
      }
    }
  }

  private void resetGameData() {
    for (final Player player : players) player.resetGameData();
  }

  private void dealHands() {
    final int CARDS_TO_DEAL = 2;
    for (int i = 0; i < PLAYERS_SEATED; i++) {
      final boolean isUser = i == 0;
      players[i].setHand(Cards.deal(this.deck, CARDS_TO_DEAL));
      if (isUser) {
        System.out.println(players[i].getNickname() + ", your hand is ");
        InfoLogger.printHandDescription(players[i]);
      }
    }
  }

  private void endGame() {
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
    roundHandler.resetPotSize();
    newGame();
  }

  private void handOutCards() {
    this.deck = Cards.getAll();
    tableCards = new ArrayList<>();
    Cards.shuffle(this.deck);
    dealHands();
  }

  public void newGame() {
    handsPlayed++;
    handOutCards();
    roundHandler.assignPositions(handsPlayed);
    roundHandler.setPreflop();
    for (int i = 0; i < ROUNDS.length; i++) {
      System.out.print(ROUNDS[i] + ": ");
      roundHandler.handle();
      Helpers.transport(this.deck, tableCards, i == 0 ? 3 : 1);
      assignCombinations();
      InfoLogger.presentTableCards();
      InfoLogger.printCombination();
    }
    InfoLogger.presentCombinations();
    winnersHandler.setPotSize(roundHandler.getPot());
    winnersHandler.handle();
    resetGameData();
    endGame();
  }
}