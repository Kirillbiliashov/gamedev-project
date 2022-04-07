public class Helpers {
  public static int randomInRange(int start, int end) {
    return (int) Math.round(Math.random() * (end - start)) + start;
  }
}
