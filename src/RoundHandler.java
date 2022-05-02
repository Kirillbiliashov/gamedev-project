import java.util.*;
import java.util.function.*;

public class RoundHandler extends Handler {
  private int currRaiseSum;
  private int playersPlayed;
  private final HashMap<Action, Consumer<Player>> actions = new HashMap<>(Action.values().length);
  private boolean isPreflop = true;
  private int bbIdx;

  public RoundHandler(final Player[] players) {
    super(players);
    this.pot = GameSession.BB_SIZE + GameSession.SB_SIZE;
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

  public void handle() {
    int currIdx = bbIdx == GameSession.PLAYERS_SEATED - 1 ? 0 : bbIdx + 1;
    while (++playersPlayed < GameSession.PLAYERS_SEATED) {
      final Player player = players[currIdx];
      final int balance = player.getBalance();
      if (player.isActive()) {
        if (balance == 0) System.out.println("Player " + player.getNickname() + " went all in");
        else {
          if (currIdx == 0) makeUserTurn(player);
          else handlePlayerAction(player);
        }
      } else System.out.println(player.getNickname() + (balance == 0 ? " sit out" : " folded"));
      if (++currIdx == GameSession.PLAYERS_SEATED) currIdx = 0;
    }
    System.out.println("Pot is " + this.pot);
    resetRoundData();
  }

  private void handlePlayerAction(final Player player) {
    final int randomDecisionNum = getRandomDecisionNum(player);
    for (final Action action : actions.keySet()) {
      if (action.getRange().contains(randomDecisionNum)) {
        actions.get(action).accept(player);
        break;
      }
    }
  }

  private int getRandomDecisionNum(final Player player) {
    final int handStrength = player.getCombination().ordinal();
    final int MIN_RANDOM_NUMBER = player.canCheck(currRaiseSum, isPreflop) ? 18 - handStrength : handStrength;
    final int MAX_RANDOM_NUMBER = player.canCheck(currRaiseSum, isPreflop) ? 30 - handStrength : 10 + handStrength;
    return Helpers.randomInRange(MIN_RANDOM_NUMBER, MAX_RANDOM_NUMBER);
  }

  private void makeUserTurn(final Player player) {
    final Action userAction = acceptActionInput(player);
    for (final Action action : actions.keySet()) {
      if (action == userAction) {
        actions.get(action).accept(player);
        break;
      }
    }
  }

  private Action acceptActionInput(final Player player) {
    final Scanner input = new Scanner(System.in);
    final int balance = player.getBalance();
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
    return userAction;
  }

  private void handleRaiseAction(final Player player) {
    final int idx = Arrays.asList(players).indexOf(player);
    final int prevRaiseSum = player.getRoundMoneyInPot();
    final int newRaiseSum;
    if (idx == 0) {
      int raiseSum = acceptRaiseSumInput();
      raiseSum = Math.min(prevRaiseSum + player.getBalance(), raiseSum);
      newRaiseSum = player.raiseFixedSum(raiseSum);
    } else newRaiseSum = player.raise(currRaiseSum);
    pot += newRaiseSum - prevRaiseSum;
    currRaiseSum = newRaiseSum;
    playersPlayed = 0;
  }

  private int acceptRaiseSumInput() {
    final Scanner input = new Scanner(System.in);
    int raiseSum;
    do {
      System.out.print("Enter raise sum: ");
      raiseSum = input.nextInt();
    } while (raiseSum < currRaiseSum);
    return raiseSum;
  }

  private void resetRoundData() {
    for (final Player player : players) player.newRound();
    this.playersPlayed = 0;
    if (isPreflop) isPreflop = false;
    this.currRaiseSum = 0;
  }

  public void setBBPosition(final int bbIdx) {
    this.bbIdx = bbIdx;
  }
  public void setPreflop() {
    this.isPreflop = true;
  }
}
