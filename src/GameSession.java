import java.util.*;

public class GameSession {
  private static final int PLAYERS_SEATED = 6;
  public static final int MIN_BALANCE = 5000;
  public static final int MAX_BALANCE = 25000;
  private static final int BB_SIZE = 100;
  private static final int SB_SIZE = 50;
  private List<Card> cards;
  private Player[] players = new Player[PLAYERS_SEATED];
  private int pot = 0;
  private int bbIdx;
  private int currRaiseSum = 0;
  private int playersPlayed = 0;

  public void start(int yourBalance, String nickname) {
    cards = Cards.getAll();
    Cards.shuffle(cards);
    for (int i = 0; i < PLAYERS_SEATED; i++) {
      int playerBalance = Helpers.randomInRange(MIN_BALANCE, MAX_BALANCE);
      boolean isUser = i == 0;
      Player player = new Player(isUser ? yourBalance : playerBalance, isUser ? nickname : "Player " + (i + 1));
      player.dealHand(cards);
      Card[] hand = player.getHand();
      if (isUser) System.out.println(nickname + ", your hand is " + hand[0].toString() + " and " + hand[1].toString());
      players[i] = player;
    }
    assignPositions();
    performBettingRound();
    System.out.println("pot is " + pot);

  }

  private void assignPositions() {
    int randomIdx = Helpers.randomInRange(0, PLAYERS_SEATED - 1);
    Player sbPlayer = players[randomIdx];
    sbPlayer.setSB();
    sbPlayer.changeBalance(-SB_SIZE);
    pot += SB_SIZE;
    players[randomIdx].setSB();
    bbIdx = randomIdx == PLAYERS_SEATED - 1 ? 0 : randomIdx + 1;
    Player bbPlayer = players[bbIdx];
    bbPlayer.setBB();
    bbPlayer.changeBalance(-BB_SIZE);
    pot += BB_SIZE;
  }

  private void performBettingRound() {
    final int MIN_RAISE_NUMBER = 8;
    final int MIN_CALL_NUMBER = 4;
    int currIdx = bbIdx == PLAYERS_SEATED - 1 ? 0 : bbIdx + 1;
    while (playersPlayed < PLAYERS_SEATED) {
      Player player = players[currIdx];
      if (!player.didFold()) {
        int randomDecisionNum = Helpers.randomInRange(0, 10);
        if (currIdx == 0) makeUserTurn(player);
        else if (currIdx == bbIdx && currRaiseSum == 0) {
          if (randomDecisionNum >= MIN_RAISE_NUMBER) raise(player);
          else System.out.println("Player " + player.getNickname() + " (big blind) checked");
        } else {
          if (randomDecisionNum < MIN_CALL_NUMBER) fold(player);
          else if (randomDecisionNum < MIN_RAISE_NUMBER) call(player);
          else raise(player);
        }
      } else System.out.println("Player " + player.getNickname() + " folded");
      if (++currIdx == PLAYERS_SEATED) currIdx = 0;
      playersPlayed++;
    }
  }

  private void raise(Player player, int raiseSum) {
    currRaiseSum = raiseSum;
    pot += raiseSum;
    player.changeBalance(-raiseSum);
    System.out.println(player.getNickname() + " raised " + raiseSum);
    playersPlayed = 0;
  }

  private void raise(Player player) {
    final int MAX_BB_SIZE_RAISE = 10;
    int randomRaiseSum = Helpers.randomInRange(SB_SIZE, BB_SIZE * MAX_BB_SIZE_RAISE);
    int roundedRaiseSum = randomRaiseSum - randomRaiseSum % SB_SIZE + currRaiseSum;
    this.raise(player, roundedRaiseSum);
  }

  private void call(Player player) {
    int callSum = currRaiseSum > 0 ? currRaiseSum : BB_SIZE;
    pot += callSum;
    player.changeBalance(-callSum);
    System.out.println("Player " + player.getNickname() + " called " + callSum);
  }

  private void fold(Player player) {
    player.setFolded();
    System.out.println("Player " + player.getNickname() + " folded");
  }

  private void makeUserTurn(Player player) {
    Scanner input = new Scanner(System.in);
    boolean userCanCheck = bbIdx == 0 && currRaiseSum == 0;
    System.out.print("It's your turn. Enter R to raise, C to " + (userCanCheck ? "check: " : "call, F to fold: "));
    char action = input.nextLine().charAt(0);
    if (userCanCheck && action == 'C') System.out.println("Player " + player.getNickname() + " checked");
    else {
      if (action == 'F') fold(player);
      else if (action == 'C') call(player);
      else if (action == 'R') {
        int raiseSum = 0;
        while (raiseSum <= BB_SIZE || raiseSum < currRaiseSum) {
          System.out.print("Enter raise sum: ");
          raiseSum = input.nextInt();
          input.nextLine();
        }
        raise(player, raiseSum);
      }
    }
  }
}




