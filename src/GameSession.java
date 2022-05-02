import java.util.*;

public final class GameSession {
  public static final int PLAYERS_SEATED = 6;
  public static final int MIN_BALANCE = 5000;
  public static final int MAX_BALANCE = 25000;
  public static final int BB_SIZE = 100;
  public static final int SB_SIZE = 50;
  private List<Card> cards;
  private static List<Card> tableCards;
  private final static Player[] players = new Player[PLAYERS_SEATED];
  private int bbIdx;
  private int handsPlayed;
  private final String[] ROUNDS = {"Preflop", "Flop", "Turn", "River" };
  private final RoundHandler roundHandler = new RoundHandler(players);
  private final WinnersHandler winnersHandler = new WinnersHandler(players);

  public void start(final int yourBalance, final String nickname) {
    if (handsPlayed == 0) {
      for (int i = 0; i < PLAYERS_SEATED; i++) {
        final boolean isUser = i == 0;
        final int playerBalance = Helpers.randomInRange(MIN_BALANCE, MAX_BALANCE);
        final Player player = new Player(isUser ? yourBalance : playerBalance, isUser ? nickname : "Player " + (i + 1));
        players[i] = player;
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
    players[sbIdx].setSB();
    players[bbIdx].setBB();
    roundHandler.setBBPosition(bbIdx);
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

  private static String handDescription(final List<Card> cards) {
    return cards.get(0).toString() + " and " + cards.get(1);
  }

  private void resetGameData() {
    for (final Player player : players) player.resetGameData();
  }

  private void dealHands() {
    for (int i = 0; i < PLAYERS_SEATED; i++) {
      final boolean isUser = i == 0;
      players[i].dealHand(cards);
      if (isUser) {
        final List<Card> hand = players[i].getHand();
        System.out.println(players[i].getNickname() + ", your hand is " + handDescription(hand));
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
  public int getUserBalance() {
    return players[0].getBalance();
  }

  private void handOutCards() {
    cards = Cards.getAll();
    tableCards = new ArrayList<>();
    Cards.shuffle(cards);
    dealHands();
  }

  public void newGame() {
    handsPlayed++;
    handOutCards();
    assignPositions();
    for (int i = 0; i < ROUNDS.length; i++) {
     roundHandler.handle();     
      Helpers.transport(cards, tableCards, i == 0 ? 3 : 1);
      assignCombinations();
      System.out.print(ROUNDS[i] + ": ");
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