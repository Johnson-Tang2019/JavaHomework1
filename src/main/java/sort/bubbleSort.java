package sort;

import main.MainWindow;
import java.util.List;

// 这个是冒泡排序的排序代码
public class bubbleSort {
  private bubbleSort() {}

  // 🌟【修改点】返回值改为 void，直接对传入的 list 原地排序
  public static void bubbleSortReturn(List<Integer> list, MainWindow mainWindow) {
    bubbleSortSortCode(list, mainWindow);
  }

  private static void bubbleSortSortCode(List<Integer> newlist, MainWindow mainWindow) {
    int size = newlist.size();
    if (size == 0) return;

    int progress = 0, nextProgress = 0;
    for (int i = 0; i < size; i++) {
      // 进度条百分比映射
      nextProgress = (i + 1) * 100 / size;
      if (nextProgress != progress) {
        if (mainWindow != null) {
          mainWindow.updateProgressSmoothly(nextProgress);
        }
      }
      progress = nextProgress;

      boolean judge = true;
      for (int j = 0; j < size - 1 - i; j++) {
        if (newlist.get(j) > newlist.get(j + 1)) {
          swap(newlist, j, j + 1);
          judge = false;
        }
      }

      if (judge) {
        if (mainWindow != null) {
          mainWindow.updateProgressSmoothly(100);
        }
        break;
      }
    }
  }

  private static void swap(List<Integer> newlist, int i, int j) {
    int temp = newlist.get(j);
    newlist.set(j, newlist.get(i));
    newlist.set(i, temp);
  }
}
