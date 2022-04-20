import java.util.*;

import enums.Combination;

public final class Player {
  private final String nickname;
  private int balance;
  private int prevBetSum;
  private List<Card> hand;
  private boolean isBB = false;
  private boolean isSB = false;
  private boolean folded = false;
  private Combination combination = Combination.HIGH_CARD;
  public Player(final int balance, final String nickname) {
    this.balance = balance;
    this.nickname = nickname;
  }

  public void dealHand(final List<Card> cards) {
    hand = Cards.deal(cards, 2);
  }

  public int getBalance() {
    return this.balance;
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

  public void setBB() {
    this.isBB = true;
    this.isSB = false;
  }

  public void setSB() {
    this.isSB = true;
    this.isBB = false;
  }

  public void setCombination(Combination combination) {
    this.combination = combination;
  }

  public void changeBalance(final int delta) {
    balance += delta;
  }

  public void setFolded(final boolean folded) {
    this.folded = folded;
  }

  public boolean didFold() {
    return this.folded;
  }

  public int raise(final int currRaiseSum) {
    final int MAX_BB_SIZE_RAISE = 10;
    final int MIN_BB_SIZE_RAISE = 2;
    final int randomRaiseSum = Helpers.randomInRange(GameSession.BB_SIZE * MIN_BB_SIZE_RAISE, GameSession.BB_SIZE * MAX_BB_SIZE_RAISE);
    final int raiseSum = randomRaiseSum - randomRaiseSum % GameSession.SB_SIZE + currRaiseSum;
    return raiseFixedSum(raiseSum);
  }

  public int raiseFixedSum(final int raiseSum) {
    final boolean isAllIn = raiseSum >= balance;
    balance -= Math.min(balance, raiseSum);
    prevBetSum = raiseSum;
    System.out.println(this.nickname + " raised " + (isAllIn ? " all in" : "") + ", balance: " + this.balance);
    return raiseSum;
  }

  public int call(final int currRaiseSum) {
    int callSum = currRaiseSum > 0 ? (currRaiseSum - prevBetSum) : GameSession.BB_SIZE;
    callSum = Math.min(this.balance, callSum);
    balance -= callSum;
    prevBetSum = callSum;
    System.out.println("Player " + this.nickname + " called " + callSum + ", balance: " + this.balance);
    return callSum;
  }

  public void fold() {
    this.folded = true;
    System.out.println("Player " + this.nickname + " folded, balance: " + this.balance);
  }

  public void resetPrevRoundData() {
    this.prevBetSum = 0;
    this.isBB = false;
    this.isSB = false;
  }
}

