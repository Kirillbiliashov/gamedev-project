public final class Helpers {
  public static int randomInRange(final int start, final int end) {
    return (int) Math.round(Math.random() * (end - start)) + start;
  }
}
