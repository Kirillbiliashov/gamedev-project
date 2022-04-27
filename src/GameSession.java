import java.util.*;
import java.util.stream.Stream;
import java.util.function.Supplier;
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
  private final String[] ROUNDS = { "Flop", "Turn", "River" };

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
      final int size = tableCards.size();
      for (int i = 0; i < size; i++) {
        System.out.print(tableCards.get(i).toString() + (i == size - 1 ? "." : ", "));
      }
      System.out.println();
    }

    public static void printCombination() {
      final Player user = players[0];
      final String combinationStr = Helpers.replaceSymbol(user.getCombination().toString(), OLD_SYMBOL, NEW_SYMBOL);
      System.out.println("Your combination is " + combinationStr.toLowerCase());
    }

    public static void presentCombinations() {
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
    pot += SB_SIZE;
    final Player bbPlayer = players[bbIdx];
    bbPlayer.setBB();
    pot += BB_SIZE;
  }

  private void handleRoundBetting() {
    final int MIN_RAISE_NUMBER = isPreflop ? 8 : 13;
    final int MIN_CALL_NUMBER = isPreflop ? 4 : 7;
    int currIdx = bbIdx == PLAYERS_SEATED - 1 ? 0 : bbIdx + 1;
    while (playersPlayed < PLAYERS_SEATED) {
      final Player player = players[currIdx];
      final boolean isBB = player.isBB();
      final int balance = player.getBalance();
      if (!player.didFold()) {
        if (balance == 0) System.out.println("Player " + player.getNickname() + " went all in");
        else {
          final int handStrength = player.getCombination().ordinal();
          final int randomDecisionNum = Helpers.randomInRange(handStrength, 10 + handStrength);
          if (currIdx == 0) makeUserTurn(player);
          else if (isPreflop ? isBB && currRaiseSum == 0 : currRaiseSum == 0) {
            if (randomDecisionNum >= MIN_RAISE_NUMBER) handleRaiseAction(player, currRaiseSum);
            else System.out.println("Player " + player.getNickname() + (isBB ? " (big blind) " : " ")
                  + "checked, balance: " + player.getBalance());
          } else {
            if (randomDecisionNum < MIN_CALL_NUMBER) player.fold();
            else if (randomDecisionNum < MIN_RAISE_NUMBER || player.getBalance() < currRaiseSum) pot += player.call(currRaiseSum);
            else handleRaiseAction(player, currRaiseSum);
          }
        }
      } else System.out.println("Player " + player.getNickname() + (player.getBalance() == 0 ? " sit out" : " folded"));
      if (++currIdx == PLAYERS_SEATED) currIdx = 0;
      playersPlayed++;
    }
    resetRoundData();
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
    if (userCanCheck && action == 'C') System.out.println("Player " + player.getNickname() + " checked, balance: " + balance);
    else {
      if (action == 'F') player.fold();
      else if (action == 'C') pot += player.call(currRaiseSum);
      else if (action == 'R') {
        if (!userCanRaise) pot += player.call(currRaiseSum);
        else {
          int raiseSum;
          do {
            System.out.print("Enter raise sum: ");
            raiseSum = input.nextInt();
          } while (raiseSum < currRaiseSum);
          handleRaiseAction(player, raiseSum);
        }
      }
    }
  }

  private void handleRaiseAction(final Player player, int raiseSum) {
    final int prevRaiseSum = player.getRoundMoneyInPot();
    final int newRaiseSum;
    if (raiseSum != currRaiseSum) {
      raiseSum = Math.min(prevRaiseSum == 0 ? player.getBalance() : player.getInitialBalance(), raiseSum);
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

  private void handleWinners() {
    final Supplier<Stream<Player>> unresolvedPlayersStream = () -> Arrays.asList(players).stream()
        .filter(player -> !player.isResolved());
    final Supplier<Stream<Player>> activeUnresolvedPlayersStream = () -> unresolvedPlayersStream.get()
        .filter(player -> !player.didFold());
    final int strongestHand = activeUnresolvedPlayersStream.get()
        .mapToInt(player -> player.getCombination().ordinal())
        .reduce(0, Math::max);
    final List<Player> winners = activeUnresolvedPlayersStream.get()
        .filter(player -> player.getCombination().ordinal() == strongestHand)
        .toList();
    final List<Player> allInWinners = winners.stream()
        .filter(winner -> winner.getBalance() == 0)
        .sorted((w1, w2) -> w1.getMoneyInPot() - w2.getMoneyInPot())
        .toList();
    if (allInWinners.size() > 0) {
      final Player allInWinner = allInWinners.get(0);
      final int winnerMoney = allInWinner.getMoneyInPot();
      final List<Player> foldedPlayers = unresolvedPlayersStream.get()
          .filter(player -> player.didFold() && player.getMoneyInPot() < winnerMoney).toList();
      final int foldSum = foldedPlayers.stream().mapToInt(Player::getMoneyInPot).reduce(0, Math::addExact);
      final int activePlayersInPot = unresolvedPlayersStream.get()
          .filter(player -> player.getMoneyInPot() >= winnerMoney).toArray().length;
      final int winSum = winnerMoney * activePlayersInPot;
      allocWinSumToWinners(winners, winSum + foldSum);
      if (pot == 0) return;
      allInWinner.setResolved();
      handleWinners();
    } else allocWinSumToWinners(winners, pot);
  }

  private void allocWinSumToWinners(final List<Player> winners, final int winSum) {
    final int winnersSize = winners.size();
    for (final Player winner : winners) winner.changeBalance(winSum / winnersSize);
    pot -= winSum;
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

  private void newGame() {
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
    handleWinners();
    resetGameData();
    endGame();
  }
}
