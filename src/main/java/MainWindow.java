import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import com.formdev.flatlaf.FlatIntelliJLaf; // 浅色现代主题
// 或者用暗黑模式：import com.formdev.flatlaf.FlatDarkLaf;

public class MainWindow {
    private JPanel mainPanel;
    private JCheckBox cbFuzzy;
    private JButton btnSearch;
    private JProgressBar progressBar1;
    private JButton btnImport;
    private JLabel label1;

    public MainWindow() {

        //监听器
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println();
            }
        });

        btnImport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("导入");
                // 1. 创建文件选择器
                JFileChooser fileChooser = new JFileChooser();

                // 2. 设置弹窗标题
                fileChooser.setDialogTitle("请选择要导入的文件");

                // 3. 过滤文件类型（可选：比如只允许导入文本文件 .txt、.csv 或图片）
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "文本与数据文件 (*.txt, *.csv)", "txt", "csv"
                );
                fileChooser.setFileFilter(filter);

                // 4. 设置默认打开的文件夹（比如用户的主目录）
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

                // 5. 弹出“打开文件”对话框 (mainPanel 是当前窗口的主面板)
                int result = fileChooser.showOpenDialog(mainPanel);

                // 6. 判断用户是否点击了“打开/确定”按钮
                if (result == JFileChooser.APPROVE_OPTION) {
                    // 获取用户选择的文件对象
                    File selectedFile = fileChooser.getSelectedFile();

                    // 获取文件的绝对路径
                    String filePath = selectedFile.getAbsolutePath();

                    // 打印到控制台看看
                    System.out.println("用户选择的文件路径是: " + filePath);


                } else {
                    // 用户点击了取消
                    label1.setText("已取消导入");
                }
            }
        });
    }

    public static void main(String[] args) {

        try {
            // 激活现代浅色主题
            FlatIntelliJLaf.setup();
            // 2. 注入全局样式：大圆角与精致字体
            UIManager.put("Component.arc", 12);
            UIManager.put("Button.arc", 16);
            UIManager.put("defaultFont", new Font("Microsoft YaHei UI", Font.PLAIN, 14));

            // 1. 给进度条设置更大的圆角（让它看起来像 iOS 或现代网页的胶囊风格）
            UIManager.put("ProgressBar.arc", 12);
            // 2. 强制规定它的最小高度（比如高度改为 8 或 12 像素）
            UIManager.put("ProgressBar.horizontalSize", new Dimension(146, 12));

            // 1. 按钮正常状态下的边框颜色（比如改成清爽的科技蓝，RGB: 52, 152, 219）
            UIManager.put("Button.startBorderColor", new Color(255, 155, 233));
            UIManager.put("Button.endBorderColor", new Color(255, 155, 233));

            // 2. 鼠标悬浮在按钮上（Hover）时的边框颜色（稍微加深一点）
            UIManager.put("Button.hoverBorderColor", new Color(255, 103, 209)); // 末尾100是半透明度

            // 3. 按钮被点击选中（Focused）时外圈的呼吸光环颜色
            UIManager.put("Component.focusColor", new Color(255, 155, 233, 100)); // 末尾100是半透明度



        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame();
        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }


    /**
     * 平滑更新进度条的值
     * @param progressBar 进度条组件
     * @param targetValue 目标百分比 (0-100)
     */
    public void updateProgressSmoothly(JProgressBar progressBar, int targetValue) {
        new Thread(() -> {
            int currentValue = progressBar.getValue();
            // 每次步进 1%，营造丝滑动画感
            while (currentValue < targetValue) {
                currentValue++;
                final int tempValue = currentValue;
                // 确保在 Swing 的事件派发线程中更新 UI
                SwingUtilities.invokeLater(() -> progressBar.setValue(tempValue));
                try {
                    Thread.sleep(10); // 控制动画速度，数字越小越快
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
