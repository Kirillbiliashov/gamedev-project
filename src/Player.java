import java.util.*;

public final class Player {
  private final String nickname;
  private int balance;
  private int roundIdx;
  private int initialBalance;
  private List<Card> hand;
  private boolean isBB = false;
  private boolean didFold = false;
  private boolean isResolved = false;
  private final int[] moneyInPot = new int[GameSession.ROUNDS_LENGTH];
  private Combination combination = Combination.HIGH_CARD;

  public Player(final int balance, final String nickname) {
    this.balance = balance;
    this.initialBalance = balance;
    this.nickname = nickname;
  }

  public void setHand(final List<Card> hand) {
    this.hand = hand;
  }

  public boolean canCheck(final int currRaiseSum, final boolean isPreflop) {
    return currRaiseSum == 0 && (!isPreflop || this.isBB);
  }

  public int getBalance() {
    return this.balance;
  }

  public int getInitialBalance() {
    return this.initialBalance;
  }

  public int getMoneyInPot() {
    int potMoneySum = 0;
    for (final int roundMoney : moneyInPot) potMoneySum += roundMoney;
    return potMoneySum;

  }

  public int getRoundMoneyInPot() {
    return this.moneyInPot[roundIdx];
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
    moneyInPot[roundIdx] += GameSession.BB_SIZE;   
    }
  }

  public void setSB() {
    if (!this.isBB) {
      this.balance -= GameSession.SB_SIZE;
      moneyInPot[roundIdx] += GameSession.SB_SIZE;
    }
  }

  public void setCombination(final Combination combination) {
    this.combination = combination;
  }

  public void changeBalance(final int delta) {
    balance += delta;
  }

  public void newRound() {
    this.roundIdx++;
  }

  public void setResolved() {
    this.isResolved = true;
  }

  public boolean isActive() {
    return !this.didFold;
  }

  public int raise(final int currRaiseSum) {
    if (currRaiseSum > this.balance) return this.call(currRaiseSum);
    final int MAX_BB_SIZE_RAISE = 10;
    final int MIN_BB_SIZE_RAISE = 2;
    final int randomRaiseSum = Helpers.randomInRange(GameSession.BB_SIZE * MIN_BB_SIZE_RAISE,
        GameSession.BB_SIZE * MAX_BB_SIZE_RAISE);
    final int raiseSum = randomRaiseSum - randomRaiseSum % GameSession.SB_SIZE + currRaiseSum;
    return raiseFixedSum(raiseSum);
  }

  public int raiseFixedSum(final int raiseSum) {
    final int delta = raiseSum - this.getRoundMoneyInPot();
    balance -= delta;
    this.moneyInPot[roundIdx] += delta;
    System.out.println(this.nickname + " raised " + (balance == 0 ? " all in" : "to " + raiseSum) + ", balance: " + this.balance);
    return raiseSum;
  }

  public int call(final int currRaiseSum) {
    final int callSum = Math.min(this.balance, currRaiseSum > 0 ? currRaiseSum - this.getRoundMoneyInPot() : GameSession.BB_SIZE);
    balance -= callSum;
    this.moneyInPot[roundIdx] += callSum;
    System.out.println("Player " + this.nickname + " called " + callSum + ", balance: " + this.balance);
    return callSum;
  }

  public void fold() {
    this.didFold = true;
    System.out.println("Player " + this.nickname + " folded, balance: " + this.balance);
  }

  public void check() {
    System.out.println("Player " + this.nickname + (this.isBB ? " (big blind) " : " ") + "checked, balance: " + this.balance);
  }

  public void resetGameData() {
    final int delta = this.balance - this.initialBalance;
    if (delta > 0) System.out.println(this.nickname + " won " + delta + ", new balance: " + this.balance);
    this.isBB = false;
    this.isResolved = false;
    this.initialBalance = this.balance;
    this.didFold = this.initialBalance == 0;
    for (int i = 0; i < moneyInPot.length; i++) this.moneyInPot[i] = 0;
    roundIdx = 0;
  }
}
