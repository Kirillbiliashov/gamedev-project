package src;

import java.util.*;
import java.util.function.*;
import enums.Action;
import staticClasses.Helpers;

public class RoundHandler extends Handler {
  private int raiseSum = 100;
  private int playersPlayed;
  private final HashMap<Action, Consumer<Player>> actions = new HashMap<>(Action.values().length);
  private boolean isPreflop = true;
  private int bigBlindIdx;

  public RoundHandler(final Player[] players) {
    super(players);
    this.pot = GameSession.BIG_BLIND_SIZE + GameSession.SMALL_BLIND_SIZE;
    this.actions.put(Action.FOLD, Player::fold);
    this.actions.put(Action.CALL, (player) -> this.pot += player.putMoneyInPot(this.raiseSum,
        Action.CALL));
    this.actions.put(Action.RAISE, (player) -> handleRaiseAction(player));
    this.actions.put(Action.CHECK, Player::check);
  }

  public int getPot() {
    return this.pot;
  }

  public void resetPotSize() {
    this.pot = GameSession.BIG_BLIND_SIZE + GameSession.SMALL_BLIND_SIZE;
  }

  public void handle() {
    final boolean isLastPlayerBigBlind = this.bigBlindIdx == GameSession.PLAYERS_SEATED - 1;
    int currIdx = isLastPlayerBigBlind ? 0 : this.bigBlindIdx + 1;
    while (this.playersPlayed++ < GameSession.PLAYERS_SEATED) {
      final Player player = players[currIdx];
      final int balance = player.getBalance();
      if (player.isActive()) {
        if (balance == 0) {
          System.out.println(player.getNickname() + " went all in");
        } else {
          if (currIdx == 0) makeUserTurn(player);
          else handlePlayerAction(player);
        }
      } else {
        final String zeroBalanceStr = balance == 0 ? " sit out" : " folded";
        System.out.println(player.getNickname() + zeroBalanceStr);
      }
      if (++currIdx == GameSession.PLAYERS_SEATED) currIdx = 0;
    }
    System.out.println("Pot is " + this.pot);
    resetRoundData();
  }

  public void assignPositions(final int handsPlayed) {
    final int randomTablePosition = Helpers.randomInRange(0,
        GameSession.PLAYERS_SEATED - 1);
    final int smallBlindIdx = handsPlayed == 1 ? randomTablePosition : this.bigBlindIdx;
    this.bigBlindIdx = smallBlindIdx == GameSession.PLAYERS_SEATED - 1 ? 0
        : smallBlindIdx + 1;
    players[smallBlindIdx].setSmallBlind();
    players[this.bigBlindIdx].setBigBlind();
  }
  
  private void handlePlayerAction(final Player player) {
    delay();
    final int randomDecisionNum = this.getRandomDecisionNum(player);
    final Set<Action> actionsKeySet = this.actions.keySet();
    for (final Action action : actionsKeySet) {
      if (action.getRange().contains(randomDecisionNum)) {
        this.actions.get(action).accept(player);
        break;
      }
    }
  }

  private void delay() {
    final int MIN_DELAY_TIME = 1000;
    final int MAX_DELAY_TIME = 3000;
    final int randomSleepTime = Helpers.randomInRange(MIN_DELAY_TIME, MAX_DELAY_TIME);
    try {
      Thread.sleep(randomSleepTime);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  private int getRandomDecisionNum(final Player player) {
    final boolean canCheck = player.canCheck(this.raiseSum, this.isPreflop);
    final int RANGE_LENGTH = canCheck ? 12 : 10;
    final int handStrength = player.getCombination().ordinal();
    final int MIN_CHECK_NUMBER = GameSession.MAX_CHECK_NUM - RANGE_LENGTH
        - handStrength;
    final int MAX_CHECK_NUMBER = GameSession.MAX_CHECK_NUM - handStrength;
    final int MIN_RANDOM_NUMBER = canCheck ? MIN_CHECK_NUMBER : handStrength;
    final int MAX_RANDOM_NUMBER = canCheck ? MAX_CHECK_NUMBER
        : RANGE_LENGTH + handStrength;
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
    final boolean canCheck = player.canCheck(this.raiseSum, this.isPreflop);
    final Action[] actionsArr = Action.values();
    try {
      final String raiseStr = balance > this.raiseSum ? "Raise, " : "";
      final String cancCheckStr = canCheck ? " or Check: " : " Call, or Fold:  ";
      System.out.print("Your balance is " + balance + ". Enter " + raiseStr +
          cancCheckStr);
      final String inputStr = input.nextLine().substring(0,
          2).toUpperCase();
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
    if (this.raiseSum > balance) {
      this.pot += player.putMoneyInPot(this.raiseSum, Action.CALL);
    } else {
      if (idx == 0) {
        this.raiseSum = acceptRaiseSumInput();
      } else {
        final int MAX_RAISE_FACTOR = 5;
        final int RAISE_SUM_LIMIT = this.raiseSum * MAX_RAISE_FACTOR;
        this.raiseSum += Helpers.randomInRange(this.raiseSum, RAISE_SUM_LIMIT,
            GameSession.SMALL_BLIND_SIZE);
      }
      this.pot += player.putMoneyInPot(this.raiseSum, Action.RAISE);
      this.playersPlayed = 1;
    }
  }

  private int acceptRaiseSumInput() {
    final Scanner input = new Scanner(System.in);
    int newRaiseSum = 0;
    do {
      try {
        System.out.print("Enter raise sum: ");
        newRaiseSum = input.nextInt();
      } catch (Exception e) {
        newRaiseSum = this.raiseSum * 2;
      }
    } while (newRaiseSum < this.raiseSum);
    return newRaiseSum;
  }

  private void resetRoundData() {
    for (final Player player : players) player.newRound();
    this.playersPlayed = 0;
    if (this.isPreflop) this.isPreflop = false;
    this.raiseSum = 100;
  }

  public void setPreflop() {
    this.isPreflop = true;
  }
}
