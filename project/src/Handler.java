package src;

public abstract class Handler {
  protected final Player[] players;
  protected int pot;

  public Handler(final Player[] players) {
    this.players = players;
  }

  public abstract void handle();
}
