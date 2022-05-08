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
  private static final String[] ROUNDS = { "Flop", "Turn", "River" };
  public static final int ROUNDS_LENGTH = ROUNDS.length;
  private final RoundHandler roundHandler = new RoundHandler(players);
  private final WinnersHandler winnersHandler = new WinnersHandler(players);

  public void start(final int yourBalance, final String nickname) {
    if (this.handsPlayed == 0) {
      final int BALANCE_ACCURACY = 5;
      for (int i = 0; i < PLAYERS_SEATED; i++) {
        final boolean isUser = i == 0;
        final int playerBalance = Helpers.randomInRangeWithAccuracy(MIN_BALANCE, MAX_BALANCE, BALANCE_ACCURACY);
        players[i] = new Player(isUser ? yourBalance : playerBalance, isUser ? nickname : "Player " + (i + 1));
      }
    }
    newGame();
  }

  private class InfoLogger {
    private final static String OLD_SYMBOL = "_";
    private final static String NEW_SYMBOL = " ";

    public static void presentTableCards() {
      for (final Card card : tableCards)
        System.out.print(card.toString() + " ");
      System.out.println();
    }

    private static void printCombination(final Combination combination) {
      final String combinationStr = Helpers.replaceSymbol(combination.toString(), OLD_SYMBOL, NEW_SYMBOL);
      System.out.println("(" + combinationStr.toLowerCase() + ")");
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
      System.out.print(hand.get(0).toString() + " and " + hand.get(1).toString());
      printCombination(player.getCombination());
    }

    private static void printUserCombination() {
      System.out.print(players[0].getNickname() + ", your hand is ");
      InfoLogger.printHandDescription(players[0]);
    }
  }

  private void assignCombinations() {
    for (final Player player : players) {
      if (player.isActive()) {
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
  }

  private void resetGameData() {
    for (final Player player : players) player.resetGameData();
  }

  private void dealHands() {
    final int CARDS_TO_DEAL = 2;
    for (int i = 0; i < PLAYERS_SEATED; i++) players[i].setHand(Cards.deal(this.deck, CARDS_TO_DEAL));
  }

  private void endGame() {
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
    if (players[0].getBalance() == 0) {
      System.out.println("Your balance is 0. Game Over!");
      return;
    }
    this.handsPlayed++;
    handOutCards();
    assignCombinations();
    InfoLogger.printUserCombination();
    roundHandler.assignPositions(this.handsPlayed);
    roundHandler.setPreflop();
    System.out.println("Preflop: ");
    roundHandler.handle();
    for (int i = 0; i < ROUNDS.length; i++) {
      Helpers.transport(this.deck, tableCards, i == 0 ? 3 : 1);
      assignCombinations();
      System.out.print(ROUNDS[i] + ": ");      
      InfoLogger.presentTableCards();
      InfoLogger.printUserCombination();
      roundHandler.handle();
    }
    InfoLogger.presentCombinations();
    winnersHandler.setPotSize(roundHandler.getPot());
    winnersHandler.handle();
    resetGameData();
    endGame();
  }
}