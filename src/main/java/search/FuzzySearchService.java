package search;

import java.util.ArrayList;
import java.util.List;

/**
 * 模糊查找服务
 * 用于在已排序的数据中进行精确查找和范围查找
 *
 * 使用方法：
 * 1. 先调用 setData 设置排序后的数据
 * 2. 调用 exactSearch 或 fuzzySearch 进行查找
 */
public class FuzzySearchService {

    private List<Integer> data;
    private boolean ready = false;

    /**
     * 设置要查找的数据（必须是已排序的）
     */
    public void setData(List<Integer> sortedData) {
        if (sortedData == null) {
            this.data = new ArrayList<>();
            this.ready = false;
        } else {
            this.data = new ArrayList<>(sortedData);
            this.ready = true;
        }
    }

    /**
     * 检查数据是否已准备好
     */
    public boolean isReady() {
        return ready && data != null && !data.isEmpty();
    }

    /**
     * 获取数据总量
     */
    public int getSize() {
        if (!ready) return 0;
        return data.size();
    }

    /**
     * 精确查找
     * @param target 要查找的数字
     * @return 查找结果
     */
    public SearchResult exactSearch(int target) {
        if (!ready) {
            return SearchResult.error("请先加载数据");
        }

        long start = System.nanoTime();
        int pos = binarySearch(target);
        double cost = (System.nanoTime() - start) / 1000000.0;

        if (pos != -1) {
            return SearchResult.exactFound(target, pos + 1, cost);
        } else {
            return SearchResult.exactNotFound(target, cost);
        }
    }

    /**
     * 模糊查找（默认范围±5）
     */
    public SearchResult fuzzySearch(int target) {
        return fuzzySearch(target, 5);
    }

    /**
     * 模糊查找（自定义范围）
     * @param target 目标数字
     * @param range 范围，如5表示查找target-5到target+5
     */
    public SearchResult fuzzySearch(int target, int range) {
        if (!ready) {
            return SearchResult.error("请先加载数据");
        }
        if (range < 0) {
            return SearchResult.error("范围不能为负数");
        }

        long start = System.nanoTime();

        int low = target - range;
        int high = target + range;

        List<Integer> values = new ArrayList<>();
        List<Integer> positions = new ArrayList<>();

        // 找到起始位置
        int startIdx = findStartIndex(low);

        // 收集范围内的数据
        for (int i = startIdx; i < data.size(); i++) {
            int val = data.get(i);
            if (val > high) {
                break;
            }
            if (val >= low && val <= high) {
                values.add(val);
                positions.add(i + 1);
            }
        }

        double cost = (System.nanoTime() - start) / 1000000.0;

        if (values.isEmpty()) {
            return SearchResult.fuzzyNotFound(target, low, high, cost);
        } else {
            return SearchResult.fuzzyFound(target, low, high, values, positions, cost);
        }
    }

    /**
     * 快速预览（用于界面实时提示）
     */
    public String quickPreview(int target, int range) {
        SearchResult res = fuzzySearch(target, range);
        if (!res.isSuccess()) {
            return res.getMessage();
        }
        if (res.getCount() == 0) {
            return "未找到 " + target + " 附近 ±" + range + " 的数字";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("找到 ").append(res.getCount()).append(" 个: ");
        List<Integer> vals = res.getValues();
        for (int i = 0; i < Math.min(5, vals.size()); i++) {
            if (i > 0) sb.append(", ");
            sb.append(vals.get(i));
        }
        if (vals.size() > 5) {
            sb.append(" 等共").append(vals.size()).append("个");
        }
        return sb.toString();
    }

    // 二分查找
    private int binarySearch(int target) {
        int left = 0, right = data.size() - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            int val = data.get(mid);
            if (val == target) {
                return mid;
            } else if (val < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return -1;
    }

    // 找到第一个 >= target 的位置
    private int findStartIndex(int target) {
        int left = 0, right = data.size() - 1;
        int result = data.size();
        while (left <= right) {
            int mid = (left + right) / 2;
            if (data.get(mid) >= target) {
                result = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        return result;
    }
}