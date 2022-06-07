package src;

import java.util.*;

import enums.Action;
import enums.Combination;

public final class Player {
  private final String nickname;
  private int balance;
  private int initialBalance;
  private List<Card> hand;
  private boolean isBigBlind = false;
  private boolean didFold = false;
  private boolean isResolved = false;
  private Combination combination = Combination.HIGH_CARD;
  private int roundMoneyInPot;
  private int moneyInPot;

  public Player(final int balance, final String nickname) {
    this.balance = balance;
    this.initialBalance = balance;
    this.nickname = nickname;
  }

  public void setHand(final List<Card> hand) {
    this.hand = hand;
  }

  public boolean canCheck(final int currRaiseSum, final boolean isPreflop) {
    return currRaiseSum == 100 && (!isPreflop || this.isBigBlind);
  }

  public boolean isActive() {
    return !this.didFold;
  }

  public int getBalance() {
    return this.balance;
  }

  public int getMoneyInPot() {
    return this.moneyInPot;
  }

  public String getNickname() {
    return this.nickname;
  }

  public List<Card> getHand() {
    return this.hand;
  }

  public Combination getCombination() {
    return this.combination;
  }

  public boolean isBigBlind() {
    return this.isBigBlind;
  }

  public boolean isUnresolved() {
    return !this.isResolved;
  }

  public void setBigBlind() {
    if (!this.isBigBlind) {
      this.isBigBlind = true;
      this.balance -= GameSession.BIG_BLIND_SIZE;
      this.roundMoneyInPot += GameSession.BIG_BLIND_SIZE;
    }
  }

  public void setSmallBlind() {
    if (!this.isBigBlind) {
      this.balance -= GameSession.SMALL_BLIND_SIZE;
      this.roundMoneyInPot += GameSession.SMALL_BLIND_SIZE;
    }
  }

  public void setCombination(final Combination combination) {
    this.combination = combination;
  }

  public void changeBalance(final int delta) {
    this.balance += delta;
  }

  public void newRound() {
    this.moneyInPot += this.roundMoneyInPot;
    this.roundMoneyInPot = 0;
  }

  public void setResolved() {
    this.isResolved = true;
  }

  public int putMoneyInPot(final int raiseSum, final Action action) {
    final boolean isRaise = action == Action.RAISE;
    if (action != Action.CALL && !isRaise) return 0;
    final int diff = Math.min(this.balance, raiseSum - this.roundMoneyInPot);
    this.balance -= diff;
    this.roundMoneyInPot += diff;
    final String allInStr = this.balance == 0 ? " (all in) " : "";
    final String actionStr = isRaise ? " raised to " + allInStr : " called " + diff;
    System.out.println(this.nickname + actionStr + ", balance: " + this.balance);
    return diff;
  }

  public void fold() {
    this.didFold = true;
    System.out.println(this.nickname + " folded, balance: " + this.balance);
  }

  public void check() {
    final String bigBlindStr = this.isBigBlind ? " (big blind) " : " ";
    System.out.println(this.nickname + bigBlindStr + "checked, balance: " + this.balance);
  }

  public void resetGameData() {
    final int delta = this.balance - this.initialBalance;
    if (delta > 0) {
      System.out.println(this.nickname + " won " + delta + ", new balance: " + this.balance);
    }
    this.isBigBlind = false;
    this.isResolved = false;
    this.initialBalance = this.balance;
    this.didFold = this.initialBalance == 0;
    this.combination = Combination.HIGH_CARD;
    this.roundMoneyInPot = 0;
    this.moneyInPot = 0;
  }
}
