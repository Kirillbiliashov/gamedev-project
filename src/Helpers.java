import java.util.*;

public final class Helpers {
  public static int randomInRange(final int start, final int end) {
    return (int) Math.round(Math.random() * (end - start)) + start;
  }

  public static <T> void transport(List<T> list1, List<T> list2, final int count) {
    for (int i = 0; i < count; i++) {
      list2.add(list1.remove(i));
    }
  }

  public static boolean hasEqualNumbers(final int[] nums, final int count) {
    int idx = 0;
    int equalNums = 1;
    while (equalNums < count) {
      if (++idx == nums.length) return false;
      if (nums[idx] == nums[idx - 1]) equalNums++;
      else equalNums = 1;
    }
    return true;
  }

  public static String replaceSymbol(final String str, final String oldStr, final String newStr) {
    final StringBuilder sb = new StringBuilder(str);
    final int idx = sb.indexOf(oldStr);
    final String replaceStr = sb.replace(idx, idx + 1, newStr).toString();
    return idx >= 0 ? replaceSymbol(replaceStr, oldStr, newStr) : str;
  }

}
