import java.util.*;

public final class Player {
  private final String nickname;
  private int balance;
  private int initialBalance;
  private List<Card> hand;
  private boolean isBB = false;
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
    return currRaiseSum == 100 && (!isPreflop || this.isBB);
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

  public boolean isBB() {
    return this.isBB;
  }

  public boolean isUnresolved() {
    return !this.isResolved;
  }

  public void setBB() {
    if (!this.isBB) {
      this.isBB = true;
      this.balance -= GameSession.BB_SIZE;
      this.roundMoneyInPot += GameSession.BB_SIZE;
    }
  }

  public void setSB() {
    if (!this.isBB) {
      this.balance -= GameSession.SB_SIZE;
      this.roundMoneyInPot += GameSession.SB_SIZE;
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
    final boolean isRaiseAction = action == Action.RAISE;
    if (action == Action.CALL || isRaiseAction) {
      final int diff = Math.min(this.balance, raiseSum - this.roundMoneyInPot);
      this.balance -= diff;
      this.roundMoneyInPot += diff;
      final String outputStr = isRaiseAction ? (" raised to " + (raiseSum + (this.balance == 0 ? " (all in) " : "")))
          : (" called " + diff);
      System.out.println(this.nickname + outputStr + ", balance: " + this.balance);
      return diff;
    }
    return 0;
  }

  public void fold() {
    this.didFold = true;
    System.out.println(this.nickname + " folded, balance: " + this.balance);
  }

  public void check() {
    System.out.println(this.nickname + (this.isBB ? " (big blind) " : " ") + "checked, balance: " + this.balance);
  }

  public void resetGameData() {
    final int delta = this.balance - this.initialBalance;
    if (delta > 0) System.out.println(this.nickname + " won " + delta + ", new balance: " + this.balance);
    this.isBB = false;
    this.isResolved = false;
    this.initialBalance = this.balance;
    this.didFold = this.initialBalance == 0;
    this.combination = Combination.HIGH_CARD;
    this.roundMoneyInPot = 0;
    this.moneyInPot = 0;
  }
}
