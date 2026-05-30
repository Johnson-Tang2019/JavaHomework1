package sort;

import java.util.ArrayList;
import java.util.List;

// 这个是归并排序的代码
public class mergeSort {
  private mergeSort() {}

  public static void mergeSortReturn(List<Integer> list) {
    mergeSortSortCode(list, 0, list.size() - 1);
  }

  private static void mergeSortSortCode(List<Integer> newList, int left, int right) {
    if (left >= right) return;
    int mid = (left + right) / 2;
    mergeSortSortCode(newList, left, mid);
    mergeSortSortCode(newList, mid + 1, right);
    merge(newList, left, mid, right);
  }

  private static void merge(List<Integer> newList, int left, int mid, int right) {
    int i = left, j = mid + 1;
    ArrayList<Integer> temp = new ArrayList<>(right - left + 1);
    while (i <= mid && j <= right) {
      if (newList.get(i) <= newList.get(j)) {
        temp.add(newList.get(i));
        i++;
      } else {
        temp.add(newList.get(j));
        j++;
      }
    }
    while (i <= mid) {
      temp.add(newList.get(i));
      i++;
    }
    while (j <= right) {
      temp.add(newList.get(j));
      j++;
    }
    for (int k = 0; k < temp.size(); k++) {
      newList.set(left + k, temp.get(k));
    }
  }
}
