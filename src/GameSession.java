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
  private int pot = 0;
  private int bbIdx;
  private int currRaiseSum = 0;
  private int playersPlayed = 0;

  public void start(final int yourBalance, final String nickname) {
    Cards.shuffle(cards);
    for (int i = 0; i < PLAYERS_SEATED; i++) {
      final int playerBalance = Helpers.randomInRange(MIN_BALANCE, MAX_BALANCE);
      final boolean isUser = i == 0;
      final Player player = new Player(isUser ? yourBalance : playerBalance, isUser ? nickname : "Player " + (i + 1));
      player.dealHand(cards);
      final Card[] hand = player.getHand();
      if (isUser) System.out.println(nickname + ", your hand is " + hand[0].toString() + " and " + hand[1].toString());
      players[i] = player;
    }
    assignPositions();
    performBettingRound();
    System.out.println("Pot is " + pot);
    Helpers.transport(cards, tableCards, 3);
    System.out.print("Flop: ");
    presentTableCards();
    Helpers.transport(cards, tableCards, 1);
    System.out.print("Turn: ");
    presentTableCards();
    System.out.print("River: ");
    Helpers.transport(cards, tableCards, 1);
    presentTableCards();


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
          else if (currIdx == bbIdx && currRaiseSum == 0) {
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
  }

  private void raise(final Player player, final int raiseSum) {
    currRaiseSum = raiseSum;
    pot += raiseSum;
    player.changeBalance(-raiseSum);
    System.out.println(player.getNickname() + " raised " + raiseSum + ", balance: " + player.getBalance());
    playersPlayed = 0;
  }

  private void raise(Player player) {
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
    final boolean userCanCheck = bbIdx == 0 && currRaiseSum == 0;
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
}