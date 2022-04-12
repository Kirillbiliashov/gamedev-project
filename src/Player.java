import java.util.*;

public class Player {
  private String nickname;
  private int balance;
  private Card[] hand = new Card[2];
  private boolean isBB = false;
  private boolean isSB = false;
  private boolean folded = false;
  public Player(int balance, String nickname) {
    this.balance = balance;
    this.nickname = nickname;
  }

  public void dealHand(List<Card> cards) {
    hand = Cards.deal(cards, 2);
  }

  public int getBalance() {
    return this.balance;
  }

  public String getNickname() {
    return this.nickname;
  }

  public Card[] getHand() {
    return this.hand;
  }

  public void setBB() {
    this.isBB = true;
    this.isSB = false;
  }

  public void setSB() {
    this.isSB = true;
    this.isBB = false;
  }

  public void changeBalance(int delta) {
    balance += delta;
  }
  public void setFolded() {
    this.folded = true;
  }
  public boolean didFold() {
    return this.folded;
  }
}
