import java.util.*;
import java.util.function.*;

public final class GameSession {
  public static final int PLAYERS_SEATED = 6;
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
  private final String[] ROUNDS = { "Flop", "Turn", "River" };
  private final HashMap<Action, Consumer<Player>> actions = new HashMap<>(Action.values().length);
  private WinnersHandler winnersHandler;

  public void start(final int yourBalance, final String nickname) {
    if (handsPlayed == 0) {
      for (int i = 0; i < PLAYERS_SEATED; i++) {
        final boolean isUser = i == 0;
        final int playerBalance = Helpers.randomInRange(MIN_BALANCE, MAX_BALANCE);
        final Player player = new Player(isUser ? yourBalance : playerBalance, isUser ? nickname : "Player " + (i + 1));
        players[i] = player;
      }
      actions.put(Action.FOLD, Player::fold);
      actions.put(Action.CALL, (player) -> pot += player.call(currRaiseSum));
      actions.put(Action.RAISE, (player) -> handleRaiseAction(player));
      actions.put(Action.CHECK, Player::check);
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
    final Player sbPlayer = players[sbIdx];
    sbPlayer.setSB();
    pot += SB_SIZE;
    final Player bbPlayer = players[bbIdx];
    bbPlayer.setBB();
    pot += BB_SIZE;
  }

  private void handleRoundBetting() {
    int currIdx = bbIdx == PLAYERS_SEATED - 1 ? 0 : bbIdx + 1;
    while (++playersPlayed < PLAYERS_SEATED) {
      final Player player = players[currIdx];
      final int balance = player.getBalance();
      if (player.isActive()) {
        if (balance == 0) System.out.println("Player " + player.getNickname() + " went all in");
        else {
          if (currIdx == 0) makeUserTurn(player);
          else {
            final int handStrength = player.getCombination().ordinal();
            final int MIN_RANDOM_NUMBER = player.canCheck(currRaiseSum, isPreflop) ? 18 - handStrength : handStrength;
            final int MAX_RANDOM_NUMBER = player.canCheck(currRaiseSum, isPreflop) ? 30 - handStrength : 10 + handStrength;
            final int randomDecisionNum = Helpers.randomInRange(MIN_RANDOM_NUMBER, MAX_RANDOM_NUMBER);
            for (final Action action : actions.keySet()) {
              if (action.getRange().contains(randomDecisionNum)) {
                actions.get(action).accept(player);
                break;
              }
            }
          }
        }
      } else System.out.println(player.getNickname() + (balance == 0 ? " sit out" : " folded"));
      if (++currIdx == PLAYERS_SEATED) currIdx = 0;
    }
    resetRoundData();
  }

  private void makeUserTurn(final Player player) {
    final int balance = player.getBalance();
    final Scanner input = new Scanner(System.in);
    Action userAction = player.canCheck(currRaiseSum, isPreflop) ? Action.CHECK : Action.CALL;
    final Action[] actionsArr = Action.values();
    try {
      System.out.print("Your balance is " + balance + ". Enter " + (balance > currRaiseSum ? "Raise" : "") +
          (player.canCheck(currRaiseSum, isPreflop) ? " or Check: " : ", Call, or Fold:  "));
      final String inputStr = input.nextLine().substring(0, 2).toUpperCase();
      for (final Action action : actionsArr) {
        if (action.toString().startsWith(inputStr)) {
          userAction = action;
          break;
        }
      }
    } catch (Exception e) {
      System.out.println("Error message: " + e.getMessage());
    }
    for (final Action action : actions.keySet()) {
      if (action == userAction) {
        actions.get(action).accept(player);
        break;
      }
    }
  }

  private void handleRaiseAction(final Player player) {
    final int idx = Arrays.asList(players).indexOf(player);
    final int prevRaiseSum = player.getRoundMoneyInPot();
    final int newRaiseSum;
    if (idx == 0) {
      final Scanner input = new Scanner(System.in);
      int raiseSum;
      do {
        System.out.print("Enter raise sum: ");
        raiseSum = input.nextInt();
      } while (raiseSum < currRaiseSum);
      raiseSum = Math.min(prevRaiseSum + player.getBalance(), raiseSum);
      newRaiseSum = player.raiseFixedSum(raiseSum);
    } else newRaiseSum = player.raise(currRaiseSum);
    pot += newRaiseSum - prevRaiseSum;
    currRaiseSum = newRaiseSum;
    playersPlayed = 0;
  }

  private void resetRoundData() {
    for (final Player player : players) player.newRound();
    playersPlayed = 0;
    if (isPreflop) isPreflop = false;
    currRaiseSum = 0;
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
    pot = 0;
    isPreflop = true;
    currRaiseSum = 0;
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
    newGame();
  }
  public int getUserBalance() {
    return players[0].getBalance();
  }

  public void newGame() {
    handsPlayed++;
    cards = Cards.getAll();
    tableCards = new ArrayList<>();
    Cards.shuffle(cards);
    dealHands();
    assignPositions();
    handleRoundBetting();
    for (int i = 0; i < ROUNDS.length; i++) {
      System.out.println("Pot is " + pot);
      Helpers.transport(cards, tableCards, i == 0 ? 3 : 1);
      assignCombinations();
      System.out.print(ROUNDS[i] + ": ");
      InfoLogger.presentTableCards();
      InfoLogger.printCombination();
      handleRoundBetting();
    }
    InfoLogger.presentCombinations();
   winnersHandler = new WinnersHandler(players, pot);
   winnersHandler.handle();
    resetGameData();
    endGame();
  }
}