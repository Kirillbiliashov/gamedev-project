import java.util.*;

public final class GameSession {
  private static final int PLAYERS_SEATED = 6;
  public static final int MIN_BALANCE = 5000;
  public static final int MAX_BALANCE = 25000;
  private static final int BB_SIZE = 100;
  private static final int SB_SIZE = 50;
  private final List<Card> cards = Cards.getAll();
  private final List<Card> tableCards = new ArrayList<>();
  private final Player[] players = new Player[PLAYERS_SEATED];
  private int pot;
  private int bbIdx;
  private int currRaiseSum;
  private int playersPlayed;
  private int prevRoundRaiseSum;
  private boolean isPreflop = true;
  private String[] rounds = { "Flop", "River", "Turn" };

  public void start(final int yourBalance, final String nickname) {
    Cards.shuffle(cards);
    for (int i = 0; i < PLAYERS_SEATED; i++) {
      final int playerBalance = Helpers.randomInRange(MIN_BALANCE, MAX_BALANCE);
      final boolean isUser = i == 0;
      final Player player = new Player(isUser ? yourBalance : playerBalance, isUser ? nickname : "Player " + (i + 1));
      player.dealHand(cards);
      final List<Card> hand = player.getHand();
      if (isUser) System.out.println(nickname + ", your hand is " + handDescription(hand));
      players[i] = player;
    }
    assignPositions();
    performBettingRound();
    System.out.println("Pot is " + pot);
    for (int i = 0; i < rounds.length; i++) {
      Helpers.transport(cards, tableCards, i == 0 ? 3 : 1);
      assignCombinations();
      System.out.print(rounds[i] + ": ");
      presentTableCards();
      printCombination();
      performBettingRound();
    }
    presentCombinations();
    decideWinner();
  }

  private void assignPositions() {
    final int randomIdx = Helpers.randomInRange(0, PLAYERS_SEATED - 1);
    final Player sbPlayer = players[randomIdx];
    sbPlayer.setSB();
    sbPlayer.changeBalance(-SB_SIZE);
    pot += SB_SIZE;
    players[randomIdx].setSB();
    bbIdx = randomIdx == PLAYERS_SEATED - 1 ? 0 : randomIdx + 1;
    final Player bbPlayer = players[bbIdx];
    bbPlayer.setBB();
    bbPlayer.changeBalance(-BB_SIZE);
    pot += BB_SIZE;
  }

  private void performBettingRound() {
    final int MIN_RAISE_NUMBER = 8;
    final int MIN_CALL_NUMBER = 4;
    int currIdx = bbIdx == PLAYERS_SEATED - 1 ? 0 : bbIdx + 1;
    while (playersPlayed < PLAYERS_SEATED) {
      final Player player = players[currIdx];
      if (!player.didFold()) {
        if (player.getBalance() == 0) System.out.println("Player " + player.getNickname() + " went al in");
        else {
          final int randomDecisionNum = Helpers.randomInRange(0, 10);
          if (currIdx == 0) makeUserTurn(player);
          else if (isPreflop ? currIdx == bbIdx && currRaiseSum == 0 : currRaiseSum == prevRoundRaiseSum) {
            if (randomDecisionNum >= MIN_RAISE_NUMBER) raise(player);
            else System.out.println("Player " + player.getNickname() + " (big blind) checked, balance: " + player.getBalance());
          } else {
            if (randomDecisionNum < MIN_CALL_NUMBER) fold(player);
            else if (randomDecisionNum < MIN_RAISE_NUMBER) call(player);
            else raise(player);
          }
        }
      } else System.out.println("Player " + player.getNickname() + " folded");
      if (++currIdx == PLAYERS_SEATED) currIdx = 0;
      playersPlayed++;
    }
    playersPlayed = 0;
    if (isPreflop) isPreflop = false;
    prevRoundRaiseSum = currRaiseSum;
  }

  private void raise(final Player player, final int raiseSum) {
    currRaiseSum = raiseSum;
    pot += raiseSum;
    player.changeBalance(-raiseSum);
    System.out.println(player.getNickname() + " raised " + raiseSum + ", balance: " + player.getBalance());
    playersPlayed = 0;
  }

  private void raise(final Player player) {
    final int MAX_BB_SIZE_RAISE = 10;
    final int balance = player.getBalance();
    if (balance < currRaiseSum) call(player, balance);
    else {
      final int randomRaiseSum = Helpers.randomInRange(SB_SIZE, BB_SIZE * MAX_BB_SIZE_RAISE);
      final int roundedRaiseSum = randomRaiseSum - randomRaiseSum % SB_SIZE + currRaiseSum;
      this.raise(player, roundedRaiseSum);
    }
  }

  private void call(final Player player) {
    int callSum = currRaiseSum > 0 ? currRaiseSum : BB_SIZE;
    callSum = Math.min(player.getBalance(), callSum);
    call(player, callSum);
  }

  private void call(final Player player, final int callSum) {
    pot += callSum;
    player.changeBalance(-callSum);
    System.out.println("Player " + player.getNickname() + " called " + callSum + ", balance: " + player.getBalance());
  }

  private void fold(final Player player) {
    player.setFolded();
    System.out.println("Player " + player.getNickname() + " folded, balance: " + player.getBalance());
  }

  private void makeUserTurn(final Player player) {
    final int balance = player.getBalance();
    final Scanner input = new Scanner(System.in);
    final boolean userCanCheck = isPreflop ? bbIdx == 0 && currRaiseSum == 0 : currRaiseSum == prevRoundRaiseSum;
    final boolean userCanRaise = balance > currRaiseSum;
    System.out.print("Your balance is " + balance + ". Enter " + (userCanRaise ? "R to raise, " : "") + "C to "
        + (userCanCheck ? "check: " : "call, F to fold: "));
    final char action = input.nextLine().charAt(0);
    if (userCanCheck && action == 'C') System.out.println("Player " + player.getNickname() + " checked, balance: " + balance);
    else {
      if (action == 'F') fold(player);
      else if (action == 'C') call(player);
      else if (action == 'R') {
        if (!userCanRaise) call(player, balance);
        else {
          int raiseSum = 0;
          while (raiseSum <= BB_SIZE || raiseSum < currRaiseSum) {
            System.out.print("Enter raise sum: ");
            raiseSum = input.nextInt();
            input.nextLine();
          }
          raiseSum = Math.min(balance, raiseSum);
          raise(player, raiseSum);
        }
      }
    }
  }

  private void presentTableCards() {
    final int size = tableCards.size();
    for (int i = 0; i < size; i++) {
      System.out.print(tableCards.get(i).toString() + (i == size - 1 ? "." : ", "));
    }
    System.out.println();
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

  private void printCombination() {
    final Player user = players[0];
    final Combination combination = user.getCombination();
    System.out.println("Your combination is " + combination.toString());
  }

  private void presentCombinations() {
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
  private String handDescription(final List<Card> cards) {
    return cards.get(0).toString() + " and " + cards.get(1);
  }
  private void decideWinner() {
   final int[] handStrengthArr = Arrays.asList(players)
   .stream()
   .filter(player -> !player.didFold())
   .mapToInt(player -> player.getCombination().ordinal())
   .sorted()
   .toArray();
   final int strongestHand = handStrengthArr[handStrengthArr.length - 1];
   for (final Player player: players) {
     if (!player.didFold() && player.getCombination().ordinal() == strongestHand) System.out.println(player.getNickname() + " won");
   }
  }
}