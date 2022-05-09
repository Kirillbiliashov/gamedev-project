package src;

import java.util.*;
import java.util.function.*;
import enums.Action;
import staticClasses.Helpers;

public class RoundHandler extends Handler {
  private int currRaiseSum = 100;
  private int playersPlayed;
  private final HashMap<Action, Consumer<Player>> actions = new HashMap<>(Action.values().length);
  private boolean isPreflop = true;
  private int bbIdx;

  public RoundHandler(final Player[] players) {
    super(players);
    this.pot = GameSession.BB_SIZE + GameSession.SB_SIZE;
    this.actions.put(Action.FOLD, Player::fold);
    this.actions.put(Action.CALL, (player) -> this.pot += player.putMoneyInPot(this.currRaiseSum, Action.CALL));
    this.actions.put(Action.RAISE, (player) -> handleRaiseAction(player));
    this.actions.put(Action.CHECK, Player::check);
  }

  public int getPot() {
    return this.pot;
  }

  public void resetPotSize() {
    pot = GameSession.BB_SIZE + GameSession.SB_SIZE;
  }

  public void handle() {
    int currIdx = this.bbIdx == GameSession.PLAYERS_SEATED - 1 ? 0 : this.bbIdx + 1;
    while (this.playersPlayed++ < GameSession.PLAYERS_SEATED) {
      final Player player = players[currIdx];
      final int balance = player.getBalance();
      if (player.isActive()) {
        if (balance == 0) System.out.println(player.getNickname() + " went all in");
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

  public void assignPositions(final int handsPlayed) {
    final int sbIdx = handsPlayed == 1 ? Helpers.randomInRange(0, GameSession.PLAYERS_SEATED - 1) : this.bbIdx;
    this.bbIdx = sbIdx == GameSession.PLAYERS_SEATED - 1 ? 0 : sbIdx + 1;
    players[sbIdx].setSB();
    players[this.bbIdx].setBB();
  }
  
  private void handlePlayerAction(final Player player) {
    final int randomDecisionNum = this.getRandomDecisionNum(player);
    final Set<Action> actionsKeySet = this.actions.keySet();
    for (final Action action : actionsKeySet) {
      if (action.getRange().contains(randomDecisionNum)) {
        this.actions.get(action).accept(player);
        break;
      }
    }
  }

  private int getRandomDecisionNum(final Player player) {
    final boolean canCheck = player.canCheck(this.currRaiseSum, this.isPreflop);
    final int RANGE_LENGTH = canCheck ? 12 : 10;
    final int handStrength = player.getCombination().ordinal();
    final int MIN_RANDOM_NUMBER = canCheck ? GameSession.MAX_CHECK_NUM - RANGE_LENGTH - handStrength : handStrength;
    final int MAX_RANDOM_NUMBER = canCheck ? GameSession.MAX_CHECK_NUM - handStrength : RANGE_LENGTH + handStrength;
    return Helpers.randomInRange(MIN_RANDOM_NUMBER, MAX_RANDOM_NUMBER);
  }

  private void makeUserTurn(final Player player) {
    final Action userAction = acceptActionInput(player);
    final Set<Action> keySet = this.actions.keySet();
    for (final Action action : keySet) {
      if (action == userAction) {
        this.actions.get(action).accept(player);
        break;
      }
    }
  }

  private Action acceptActionInput(final Player player) {
    final Scanner input = new Scanner(System.in);
    final int balance = player.getBalance();
    final boolean canCheck = player.canCheck(this.currRaiseSum, this.isPreflop);
    final Action[] actionsArr = Action.values();
    try {
      System.out.print("Your balance is " + balance + ". Enter " + (balance > this.currRaiseSum ? "Raise, " : "") +
          (canCheck ? " or Check: " : " Call, or Fold:  "));
      final String inputStr = input.nextLine().substring(0, 2).toUpperCase();
      for (final Action action : actionsArr) {
        if (action.toString().startsWith(inputStr)) return action;
      }
    } catch (Exception e) {
      System.out.println("Error message: " + e.getMessage());
    }
    return canCheck ? Action.CHECK : Action.CALL;
  }

  private void handleRaiseAction(final Player player) {
    final int idx = Arrays.asList(players).indexOf(player);
    final int balance = player.getBalance();
    if (this.currRaiseSum > balance) this.pot += player.putMoneyInPot(this.currRaiseSum, Action.CALL);
    else {
      if (idx == 0) this.currRaiseSum = acceptRaiseSumInput();
      else {
        final int MAX_RAISE_FACTOR = 5;
        this.currRaiseSum += Helpers.randomInRangeWithAccuracy(this.currRaiseSum, this.currRaiseSum * MAX_RAISE_FACTOR,
            GameSession.SB_SIZE);
      }
      this.pot += player.putMoneyInPot(this.currRaiseSum, Action.RAISE);
      this.playersPlayed = 1;
    }
  }

  private int acceptRaiseSumInput() {
    final Scanner input = new Scanner(System.in);
    int raiseSum = 0;
    do {
      try {
        System.out.print("Enter raise sum: ");
        raiseSum = input.nextInt();
      } catch (Exception e) {
        raiseSum = this.currRaiseSum * 2;
      }
    } while (raiseSum < this.currRaiseSum);
    return raiseSum;
  }

  private void resetRoundData() {
    for (final Player player : players) player.newRound();
    this.playersPlayed = 0;
    if (this.isPreflop) this.isPreflop = false;
    this.currRaiseSum = 100;
  }

  public void setPreflop() {
    this.isPreflop = true;
  }
}
