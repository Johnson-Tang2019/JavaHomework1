package search;

import java.util.ArrayList;
import java.util.List;

/** 查找结果封装 包含查找结果的各种信息和展示方法 */
public class SearchResult {

  private boolean success;
  private boolean found;
  private String type;
  private String msg;
  private String detail;
  private double timeMs;

  private int target;
  private int low;
  private int high;

  private List<Integer> values;
  private List<Integer> positions;

  // 精确查找 - 找到
  public static SearchResult exactFound(int target, int pos, double time) {
    SearchResult r = new SearchResult();
    r.success = true;
    r.found = true;
    r.type = "精确查找";
    r.target = target;
    r.timeMs = time;
    r.values = new ArrayList<>();
    r.positions = new ArrayList<>();
    r.values.add(target);
    r.positions.add(pos);
    r.msg = "数字 " + target + " 在第 " + pos + " 位";
    r.detail =
        "精确查找成功\n目标: " + target + "\n位置: 第" + pos + "位\n耗时: " + String.format("%.3f", time) + " ms";
    return r;
  }

  // 精确查找 - 未找到
  public static SearchResult exactNotFound(int target, double time) {
    SearchResult r = new SearchResult();
    r.success = true;
    r.found = false;
    r.type = "精确查找";
    r.target = target;
    r.timeMs = time;
    r.values = new ArrayList<>();
    r.positions = new ArrayList<>();
    r.msg = "未找到数字 " + target;
    r.detail = "精确查找失败\n目标: " + target + "\n耗时: " + String.format("%.3f", time) + " ms";
    return r;
  }

  // 模糊查找 - 找到
  public static SearchResult fuzzyFound(
      int target, int low, int high, List<Integer> vals, List<Integer> poss, double time) {
    SearchResult r = new SearchResult();
    r.success = true;
    r.found = true;
    r.type = "模糊查找";
    r.target = target;
    r.low = low;
    r.high = high;
    r.timeMs = time;
    r.values = new ArrayList<>(vals);
    r.positions = new ArrayList<>(poss);

    r.msg = "在 [" + low + ", " + high + "] 中找到 " + vals.size() + " 个数字";

    StringBuilder sb = new StringBuilder();
    sb.append("模糊查找成功\n");
    sb.append("目标: ").append(target).append("\n");
    sb.append("范围: [").append(low).append(", ").append(high).append("]\n");
    sb.append("匹配数: ").append(vals.size()).append("\n");
    sb.append("耗时: ").append(String.format("%.3f", time)).append(" ms\n");

    if (!vals.isEmpty()) {
      sb.append("\n详情:\n");
      int show = Math.min(15, vals.size());
      for (int i = 0; i < show; i++) {
        sb.append("  ").append(vals.get(i)).append(" → 第").append(poss.get(i)).append("位\n");
      }
      if (vals.size() > show) {
        sb.append("  ... 还有").append(vals.size() - show).append("个\n");
      }
    }
    r.detail = sb.toString();
    return r;
  }

  // 模糊查找 - 未找到
  public static SearchResult fuzzyNotFound(int target, int low, int high, double time) {
    SearchResult r = new SearchResult();
    r.success = true;
    r.found = false;
    r.type = "模糊查找";
    r.target = target;
    r.low = low;
    r.high = high;
    r.timeMs = time;
    r.values = new ArrayList<>();
    r.positions = new ArrayList<>();
    r.msg = "在 [" + low + ", " + high + "] 中未找到匹配数字";
    r.detail =
        "模糊查找失败\n目标: "
            + target
            + "\n范围: ["
            + low
            + ", "
            + high
            + "]\n耗时: "
            + String.format("%.3f", time)
            + " ms";
    return r;
  }

  // 错误结果
  public static SearchResult error(String errMsg) {
    SearchResult r = new SearchResult();
    r.success = false;
    r.found = false;
    r.type = "错误";
    r.msg = errMsg;
    r.detail = "错误: " + errMsg;
    r.values = new ArrayList<>();
    r.positions = new ArrayList<>();
    r.timeMs = 0;
    return r;
  }

  // Getter方法
  public boolean isSuccess() {
    return success;
  }

  public boolean isFound() {
    return found;
  }

  public String getType() {
    return type;
  }

  public String getMessage() {
    return msg;
  }

  public String getDetail() {
    return detail;
  }

  public double getTime() {
    return timeMs;
  }

  public int getTarget() {
    return target;
  }

  public int getLow() {
    return low;
  }

  public int getHigh() {
    return high;
  }

  public int getCount() {
    return values.size();
  }

  public Integer getFirstValue() {
    return values.isEmpty() ? null : values.get(0);
  }

  public Integer getFirstPos() {
    return positions.isEmpty() ? null : positions.get(0);
  }

  public List<Integer> getValues() {
    return new ArrayList<>(values);
  }

  public List<Integer> getPositions() {
    return new ArrayList<>(positions);
  }

  @Override
  public String toString() {
    return detail;
  }
}
