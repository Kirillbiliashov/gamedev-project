public enum Suit {
  CLUBS("♣️"),
  DIAMONDS("♦️"),
  SPADES("♠️"),
  HEARTS("♥️");

  private String emoji;

  private Suit(final String emoji) {
    this.emoji = emoji;
  }

  public String getEmoji() {
    return this.emoji;
  }
}