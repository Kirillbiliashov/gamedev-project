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

  public void start(int yourBalance, String nickname) {
    cards = Cards.getAll();
    Cards.shuffle(cards);
    for (int i = 0; i < PLAYERS_SEATED; i++) {
      int playerBalance = Helpers.randomInRange(MIN_BALANCE, MAX_BALANCE);
      boolean isYou = i == 0;
      Player player = new Player(isYou ? yourBalance : playerBalance, isYou ? nickname : "Player " + (i + 1));
      player.dealHand(cards);
      Card[] hand = player.getHand();
      if (isYou) System.out.println(nickname + ", your hand is " + hand[0].toString() + " and " + hand[1].toString());
      players[i] = player;
    }
  }

  private void assignPositions() {
    //To be written soon...
  }
}
