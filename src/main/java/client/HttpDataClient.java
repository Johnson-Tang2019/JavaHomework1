package client;

import main.MainWindow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 进阶版 数据采集客户端 功能：通过 HTTP 协议从 Java 内置 HTTP 服务器读取本地文本数据 包含：网络请求、异常处理、数据解析、数据打印、数据统计 适用：Java 网络程序设计 -
 * 进阶数据采集作业
 */
public class HttpDataClient {

  private final MainWindow mainWindow;

  public HttpDataClient(MainWindow mainWindow) {
    this.mainWindow = mainWindow;
  }

  /**
   * 从 HTTP 服务器读取数据（核心方法） 功能：发送 GET 请求，接收纯文本数据，解析为整数列表
   *
   * @param urlStr 服务器数据接口地址
   * @return 解析后的整数集合，读取失败返回 null
   */
  public static List<Integer> readDataFromServer(String urlStr) {
    // 用于存储最终解析的整数数据
    List<Integer> dataList = new ArrayList<>();

    // 网络连接与流对象（定义在try外，方便finally关闭）
    HttpURLConnection connection = null;
    BufferedReader reader = null;

    try {
      // 1. 创建 URL 对象，代表网络资源地址
      URL url = new URL(urlStr);

      // 2. 打开 HTTP 连接，强制转换为 HttpURLConnection
      connection = (HttpURLConnection) url.openConnection();

      // 3. 配置 HTTP 请求参数
      connection.setRequestMethod("GET"); // 请求方式：GET
      connection.setConnectTimeout(5000); // 连接超时时间：5秒
      connection.setReadTimeout(10000); // 读取超时时间：10秒
      connection.setRequestProperty("Accept", "text/plain"); // 接收文本类型数据

      // 4. 获取服务器响应状态码，判断请求是否成功（200=成功）
      int responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        throw new IOException("服务器响应异常，状态码：" + responseCode);
      }

      // 5. 获取服务器输入流，用于读取返回数据
      // 使用 UTF-8 编码读取，避免中文乱码
      reader =
          new BufferedReader(
              new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

      String line;
      // 6. 逐行读取服务器返回的文本内容
      while ((line = reader.readLine()) != null) {
        line = line.trim(); // 去除每行前后空格

        // 跳过空行，避免解析错误
        if (!line.isEmpty()) {
          // 将字符串转为整数，加入数据集合
          dataList.add(Integer.parseInt(line));
        }
      }

      // ===================== 异常处理 =====================
    } catch (SocketTimeoutException e) {
      // 网络超时异常（服务器未启动/网络不通）
      System.err.println("[ERROR]错误：网络请求超时，请检查服务器是否启动");
      return null;
    } catch (IOException e) {
      // IO异常（连接失败、文件不存在、服务器异常）
      System.err.println("[ERROR]错误：网络请求失败 - " + e.getMessage());
      return null;
    } catch (NumberFormatException e) {
      // 数据格式异常（服务器返回的不是整数）
      System.err.println("[ERROR]错误：服务器返回的数据不是有效整数 - " + e.getMessage());
      return null;

      // ===================== 资源释放 =====================
    } finally {
      // 无论是否异常，都必须关闭流和连接，防止资源泄漏
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (connection != null) {
        connection.disconnect();
      }
    }

    // 返回解析完成的数据集合
    return dataList;
  }

  /**
   * 数据预览打印功能 功能：只打印前 N 条和后 N 条，避免大量数据刷屏
   *
   * @param dataList 采集到的数据集合
   * @param previewCount 前后各显示多少条数据
   */
  public static String printDataPreview(List<Integer> dataList, int previewCount) {

    String result = "\n[INFO] 数据预览：";

    StringBuilder sb = new StringBuilder(result);

    // 打印前 N 条数据
    sb.append("\n前" + previewCount + "条数据：");
    for (int i = 0; i < Math.min(previewCount, dataList.size()); i++) {
      sb.append(String.format("  [%d] = %d%n", i, dataList.get(i)));
    }

    // 如果数据总量 > 2*预览条数，显示中间省略，并打印最后 N 条
    if (dataList.size() > 2 * previewCount) {
      sb.append("\n  ... （中间省略 " + (dataList.size() - 2 * previewCount) + " 条数据）");

      // 打印后 N 条数据
      sb.append("\n后" + previewCount + "条数据：");
      for (int i = dataList.size() - previewCount; i < dataList.size(); i++) {
        sb.append(String.format("  [%d] = %d%n", i, dataList.get(i)));
      }
    }

    return sb.toString();
  }

  /**
   * 数据统计功能 功能：计算并输出 最大值、最小值、平均值
   *
   * @param dataList 采集到的数据集合
   */
  public static void calculateAndPrintStats(List<Integer> dataList, MainWindow mainWindow) {
    // 初始化变量
    int max = Integer.MIN_VALUE; // 最大值初始化为最小整数
    int min = Integer.MAX_VALUE; // 最小值初始化为最大整数
    long sum = 0; // 数据总和（使用long防止溢出）

    // 遍历集合，计算最大、最小、总和
    for (int num : dataList) {
      if (num > max) max = num;
      if (num < min) min = num;
      sum += num;
    }

    // 计算平均值
    double average = (double) sum / dataList.size();

    // 输出统计结果
    mainWindow.appendLog("\n[INFO] 数据统计信息：");
    mainWindow.appendLog("  最大值：" + max);
    mainWindow.appendLog("  最小值：" + min);
    mainWindow.appendLog(String.format("  平均值：%.2f%n", average));
  }

  /** 程序主入口 执行流程：设置服务器地址 → 读取数据 → 打印结果 → 数据统计 */
  public void HttpSort(int port, int sortType, MainWindow mainWindow) {
    // 定义服务器请求地址（必须与服务端端口保持一致）
    String url = "http://localhost:" + port + "/data";

    // 控制台输出程序启动提示
    mainWindow.appendLog("=== 进阶版数据采集程序 ===");
    mainWindow.appendLog("正在从服务器读取数据...\n");

    // 调用方法，从 HTTP 服务器读取数据，返回整数集合
    List<Integer> dataList = readDataFromServer(url);

    // 判断数据是否读取成功
    if (dataList != null && !dataList.isEmpty()) {
      // 读取成功：输出提示信息与数据总量
      mainWindow.appendLog("[INFO] 进阶版数据采集成功！");
      mainWindow.appendLog("[INFO] 数据总数：" + dataList.size() + " 条");

      // ===================== 拓展功能 =====================
      // 打印数据预览（前10条 + 后10条）
      printDataPreview(dataList, 10);
      // 计算并打印统计信息（最大值、最小值、平均值）
      calculateAndPrintStats(dataList, mainWindow);
      // ========= 排序耗时测试与日志 =========
      // 因为有冒泡排序，所以排序较慢
      testSortingPerformance(dataList, sortType);
    } else {
      // 读取失败：输出错误提示
      mainWindow.appendLog("\n[ERROR] 数据采集失败，请检查服务器是否启动");
    }
  }

  /** 测试三种排序算法的性能并记录到日志文件 */
  private void testSortingPerformance(List<Integer> dataList, int sortType) {
    mainWindow.appendLog("\n⏱️ 排序开始（升序排序耗时）");
    List<String> logLines = new ArrayList<>();
    new Thread(
            () -> {
              if (sortType == 2) {
                // 冒泡排序
                long start = System.nanoTime();
                sort.bubbleSort.bubbleSortReturn(dataList, mainWindow);
                long end = System.nanoTime();
                double bubbleMs = (end - start) / 1_000_000.0;
                String bubbleLog = String.format("冒泡排序耗时: %.3f ms", bubbleMs);
                mainWindow.updateProgressSmoothly(100);
                mainWindow.appendLog(bubbleLog);
                logLines.add(bubbleLog);
                mainWindow.appendLog("\n冒泡排序后前5个元素：");
                for (int i = 0; i < Math.min(5, dataList.size()); i++) {
                  mainWindow.appendLog(dataList.get(i) + "， ");
                }
                mainWindow.appendLog("\n");

              } else if (sortType == 0) {
                // 快速排序
                long start = System.nanoTime();
                sort.quickSort.quickSortReturn(dataList);
                long end = System.nanoTime();
                double quickMs = (end - start) / 1_000_000.0;
                String quickLog = String.format("快速排序耗时: %.3f ms", quickMs);
                mainWindow.updateProgressSmoothly(100);
                mainWindow.appendLog(quickLog);
                logLines.add(quickLog);
                // 简单验证排序正确性（可选）
                mainWindow.appendLog("\n快速排序后前5个元素：");
                for (int i = 0; i < Math.min(5, dataList.size()); i++) {
                  mainWindow.appendLog(dataList.get(i) + " ");
                }
                mainWindow.appendLog("\n");

              } else if (sortType == 1) {
                // 归并排序
                mainWindow.updateProgressSmoothly(60);
                long start = System.nanoTime();
                sort.mergeSort.mergeSortReturn(dataList);
                long end = System.nanoTime();
                double mergeMs = (end - start) / 1_000_000.0;
                String mergeLog = String.format("归并排序耗时: %.3f ms", mergeMs);
                mainWindow.updateProgressSmoothly(100);
                mainWindow.appendLog(mergeLog);
                logLines.add(mergeLog);
                mainWindow.appendLog("\n归并排序后前5个元素：");
                for (int i = 0; i < Math.min(5, dataList.size()); i++) {
                  mainWindow.appendLog(dataList.get(i) + "， ");
                }
                mainWindow.appendLog("\n");
              }

              mainWindow.dataList = dataList;

              // 写入日志文件（追加模式）
              try (java.io.FileWriter fw = new java.io.FileWriter("sort_times.log", true);
                  java.io.PrintWriter pw = new java.io.PrintWriter(fw)) {
                pw.println("=== " + new java.util.Date() + " ===");
                for (String line : logLines) {
                  pw.println(line);
                }
                pw.println(); // 空行
                mainWindow.appendLog(
                    "[INFO]排序耗时已追加到日志文件: " + new java.io.File("sort_times.log").getAbsolutePath());
              } catch (IOException e) {
                mainWindow.appendLog("[ERROR]写入日志文件失败: " + e.getMessage());
              }
            })
        .start();
  }
}
