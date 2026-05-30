package sort;

import java.util.ArrayList;
import java.util.List;

// 这是快速排序的代码
public class quickSort {
  private quickSort() {}

  public static void quickSortReturn(List<Integer> list) {
    quickSortCode(list, 0, list.size() - 1);
  }

  private static void quickSortCode(List<Integer> newList, int low, int high) {
    if (low >= high) return;
    int compare = newList.get(high);
    int i = low, j = low;
    for (; i < high; i++) {
      if (newList.get(i) < compare) {
        swap(newList, i, j);
        j++;
      }
    }
    swap(newList, i, j);
    quickSortCode(newList, low, j - 1);
    quickSortCode(newList, j + 1, high);
  }

  private static void swap(List<Integer> newlist, int i, int j) {
    int temp = newlist.get(j);
    newlist.set(j, newlist.get(i));
    newlist.set(i, temp);
  }
}
