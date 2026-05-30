package main;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import client.HttpDataClient;
import com.formdev.flatlaf.FlatIntelliJLaf;

import search.FuzzySearchService;
import search.SearchResult;
import server.SimpleDataServer;

public class MainWindow {

  public List<Integer> dataList = new ArrayList<>();
  // 基础业务变量
  private int searchType = 0;
  /*
   * 排序类型
   * 0：快速排序
   * 1：归并排序
   * 2：冒泡排序
   */
  private boolean fuzzySearch = false;
  private int port = 60001;
  private boolean isServerRunning = false;
  // UI 组件（确保这些变量名与你的 .form 界面文件对齐）
  private JPanel mainPanel;
  private JCheckBox cbFuzzy;
  private JButton btnSearch;
  private JProgressBar progressBar1;
  private JButton btnImport;
  private JComboBox<String> comboBox1; // 加上泛型更严谨
  private JTextField tgSearch;
  private JTextArea logArea;
  private JTextField tfPort;
  private JButton btnSort;

  public MainWindow() {

    if (progressBar1 != null) {
      progressBar1.setOpaque(true);
      progressBar1.setBorderPainted(false);
      progressBar1.setBackground(new Color(248, 242, 246)); // 淡灰粉底色
      progressBar1.setForeground(new Color(255, 155, 233)); // 樱花粉前景色
    }

    // --- 1. 搜索按钮监听 ---
    btnSearch.addActionListener(
        _ -> {
          if (dataList.isEmpty()) {
            appendLog("[WARN] 数据源为空！请先排序。");
            return;
          }

          // 1. 获取输入框的文本
          String searchKey = tgSearch.getText().trim();

          if (searchKey.isEmpty()) {
            appendLog("[WARN] 检索关键词不能为空！");
            return;
          }

          // 2. 界面状态即时反馈
          if (progressBar1 != null) progressBar1.setValue(0);
          appendLog(
              String.format("[INFO] 开始检索. 模式: %s, 关键词: %s", fuzzySearch ? "模糊" : "精确", searchKey));

          // 3. 开启后台异步线程，防止搜索和解析数据时界面假死
          new Thread(
                  () -> {
                    SearchResult result;

                    try {
                      if (fuzzySearch) {
                        FuzzySearchService fuzzySearchService = new FuzzySearchService();
                        fuzzySearchService.setData(dataList);
                        result = fuzzySearchService.fuzzySearch(Integer.parseInt(searchKey));

                      } else {
                        FuzzySearchService fuzzySearchService = new FuzzySearchService();
                        fuzzySearchService.setData(dataList);
                        result = fuzzySearchService.exactSearch(Integer.parseInt(searchKey));
                      }

                    } catch (NumberFormatException ex) {
                      result = SearchResult.error("请输入有效的整数关键词！");
                    } catch (Exception ex) {
                      result = SearchResult.error("检索中途出现未知异常: " + ex.getMessage());
                    }

                    // 4. 🔥【UI 线程安全投递】将刚才算好的 SearchResult 送回主界面展示
                    final SearchResult finalResult = result;
                    SwingUtilities.invokeLater(
                        () -> {
                          // 通过重写好的 toString() 或者 getDetail() 将唯美排版打印出来
                          appendLog(finalResult.getDetail());

                          if (progressBar1 != null) progressBar1.setValue(100);
                        });
                  })
              .start(); // 启动线程！
        });

    // --- 2. 导入按钮监听 ---
    btnImport.addActionListener(
        _ -> {
          if (isServerRunning) appendLog("[WARN] 服务器已运行！。");
          else {
            // 🛡️ 1. 安全提取并校验端口号，彻底解决不敲回车不生效和乱输入崩溃的问题
            int currentPort;
            try {
              String portText = tfPort.getText().trim();
              currentPort = Integer.parseInt(portText);

              // 端口范围合法性校验 (0 ~ 65535)
              if (currentPort < 1024 || currentPort > 65535) {
                appendLog("[WARN] 端口号超出安全范围！请输入 1024 ~ 65535 之间的数字。");
                return;
              } else {
                port = currentPort;
              }
            } catch (NumberFormatException ex) {
              appendLog("[WARN] 端口号格式错误！请输入纯数字。");
              return;
            }

            // 2. 打开文件选择器
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("请选择要导入的文件");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("文本与数据文件 (*.txt)", "txt");
            fileChooser.setFileFilter(filter);
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

            int result = fileChooser.showOpenDialog(mainPanel);

            if (result == JFileChooser.APPROVE_OPTION) {
              File selectedFile = fileChooser.getSelectedFile();
              String filePath = selectedFile.getAbsolutePath();

              appendLog("[INFO] 成功选择文件: " + filePath);

              // 开启后台线程异步启动服务器
              new Thread(
                      () -> {
                        try {
                          updateProgressSmoothly(60);

                          // 🔥【核心联动】动态传入刚刚获取并校验通过的最新端口号
                          // 这样即使用户在界面的输入框里临时改这里也能实时抓这里也能实时抓取到
                          SimpleDataServer server = new SimpleDataServer(MainWindow.this);
                          server.serverStart(filePath, port);

                          updateProgressSmoothly(100);
                          isServerRunning = true;
                          SwingUtilities.invokeLater(() -> appendLog("[SUCCESS] 数据文件导入成功，服务器已就绪。"));

                        } catch (Exception ex) {
                          SwingUtilities.invokeLater(
                              () -> {
                                progressBar1.setValue(0);
                                appendLog("[ERROR] 服务器启动失败(可能端口被占用): " + ex.getMessage());
                              });
                        }
                      })
                  .start();

            } else {
              appendLog("[INFO] 用户取消了导入操作。");
            }
          }
        });

    // --- 3. 下拉框监听 ---
    comboBox1.addActionListener(
        _ -> {
          searchType = comboBox1.getSelectedIndex();
          appendLog("[CONFIG] 搜索类型切换为: " + searchType);
        });
    comboBox1.setSelectedIndex(0);

    // --- 4. 模糊勾选框监听 ---
    cbFuzzy.addActionListener(
        _ -> {
          fuzzySearch = cbFuzzy.isSelected();
          appendLog("[CONFIG] 模糊检索开关: " + (fuzzySearch ? "开启" : "关闭"));
        });

    // 注入现代感十足的灰色占位提示词
    tgSearch.putClientProperty("JTextField.placeholderText", "请输入检索关键词...");

    tfPort.setText("60001");

    // 排序按钮监听
    btnSort.addActionListener(
        _ -> {
          HttpDataClient client = new HttpDataClient(MainWindow.this);
          client.HttpSort(port, searchType, MainWindow.this);
        });
  }

  public static void main(String[] args) {
    // 🌟 核心修正：利用 invokeLater 将所有 UI 渲染与窗体显示彻底隔离开，绝不阻塞主线程
    SwingUtilities.invokeLater(
        () -> {
          try {
            // 1. 激活现代浅色主题
            FlatIntelliJLaf.setup();

            // 2. 全局通用圆角和字体微调
            UIManager.put("Component.arc", 12);
            UIManager.put("ComboBox.arc", 12);
            UIManager.put("Button.arc", 16);
            UIManager.put("defaultFont", new Font("Microsoft YaHei UI", Font.PLAIN, 14));

            // ===================== 🌸 百分之百不翻车的进度条配置 🌟 =====================
            UIManager.put("ProgressBar.arc", 12); // 保持圆角

            // 使用标准 Color 对象注入前景色（樱花粉）
            UIManager.put("ProgressBar.foreground", new Color(255, 155, 233));

            // 槽位背景色：淡灰粉底色
            UIManager.put("ProgressBar.background", new Color(248, 192, 239));

            // 彻底锁死文字颜色，防止其因为状态改变而变白或变黑导致看不清
            UIManager.put("ProgressBar.selectionForeground", new Color(62, 89, 123)); // 未覆盖时的文字色

            // 3. 唯美粉粉紫边框色调调配
            UIManager.put("Button.startBorderColor", new Color(255, 155, 233));
            UIManager.put("Button.endBorderColor", new Color(255, 155, 233));
            UIManager.put("Button.hoverBorderColor", new Color(255, 103, 209));
            UIManager.put("Component.focusColor", new Color(255, 155, 233, 100));

          } catch (Exception e) {
            e.printStackTrace();
          }

          // 4. 🌟 在安全线程内部实例化窗体，消灭“有时可以，有时不行”的玄学 Bug
          JFrame frame = new JFrame("数据检索系统");
          MainWindow window = new MainWindow();

          if (window.mainPanel == null) {
            System.out.println("❌ 警告：mainPanel 为 null！请检查 .form 文件绑定或点击 Build -> Rebuild Project");
          }

          frame.setContentPane(window.mainPanel);
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          frame.pack();
          frame.setLocationRelativeTo(null); // 让窗口居中显示
          frame.setVisible(true); // 100% 稳定弹窗！
        });
  }

  /** 【重要升级】线程安全地向 GUI 文本域追加日志，并带自动截断保护 */
  public void appendLog(String message) {
    if (logArea == null) return;

    // 确保跨线程调用时也是 UI 安全的
    SwingUtilities.invokeLater(
        () -> {
          logArea.append(message + "\n");

          // 内存保护：最多保留 300 行日志，超过就删掉头部，防止 OOM
          int maxLines = 300;
          if (logArea.getLineCount() > maxLines) {
            try {
              int endOffset = logArea.getLineStartOffset(logArea.getLineCount() - maxLines);
              logArea.replaceRange("", 0, endOffset);
            } catch (Exception ignored) {
            }
          }
          // 自动滚动到最后一行
          logArea.setCaretPosition(logArea.getDocument().getLength());
        });
  }

  /** 平滑更新进度条的值 */
  public void updateProgressSmoothly(int targetValue) {
    // 重置进度条
    if (targetValue < progressBar1.getValue())
      SwingUtilities.invokeLater(() -> progressBar1.setValue(0));
    else if (progressBar1.getValue() == 100) {
      progressBar1.setValue(1);
    }

    new Thread(
            () -> {
              // 如果目标值比当前值小，说明是新一轮进度，先强行归零
              if (targetValue < progressBar1.getValue()) {
                SwingUtilities.invokeLater(() -> progressBar1.setValue(0));
                try {
                  Thread.sleep(15);
                } catch (InterruptedException ignored) {
                }
              }

              int currentValue = progressBar1.getValue();
              while (currentValue < targetValue) {
                currentValue++;
                final int tempValue = currentValue;
                SwingUtilities.invokeLater(() -> progressBar1.setValue(tempValue));
                try {
                  Thread.sleep(6); // 6ms 的步进速度最契合 146px 宽度的粉蓝平滑视觉
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              }
            })
        .start();
  }
}
