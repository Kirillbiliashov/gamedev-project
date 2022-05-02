import java.util.*;
import java.util.function.*;

public class RoundHandler {
  private final Player[] players;
  private int currRaiseSum;
  private int playersPlayed;
  private final HashMap<Action, Consumer<Player>> actions = new HashMap<>(Action.values().length);
  private int pot = GameSession.BB_SIZE + GameSession.SB_SIZE;
  private boolean isPreflop = true;
  private int bbIdx;

  public RoundHandler(final Player[] players) {
    this.players = players;
    actions.put(Action.FOLD, Player::fold);
    actions.put(Action.CALL, (player) -> this.pot += player.call(currRaiseSum));
    actions.put(Action.RAISE, (player) -> handleRaiseAction(player));
    actions.put(Action.CHECK, Player::check);
  }

  public int getPot() {
    return this.pot;
  }
  public void resetPotSize() {
    pot = GameSession.BB_SIZE + GameSession.SB_SIZE;
  }

  public void performRoundBetting() {
    int currIdx = bbIdx == GameSession.PLAYERS_SEATED - 1 ? 0 : bbIdx + 1;
    while (++playersPlayed < GameSession.PLAYERS_SEATED) {
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
      if (++currIdx == GameSession.PLAYERS_SEATED) currIdx = 0;
    }
    System.out.println("Pot is " + this.pot);
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

  public void setBBPosition(final int bbIdx) {
    this.bbIdx = bbIdx;
  }
}
