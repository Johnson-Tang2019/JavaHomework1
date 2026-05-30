package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

import main.MainWindow;

/** Java 内置 HTTP 服务器（仅用于提供 data.txt 数据） 端口：60001 访问地址：http://localhost:60001/data */
public class SimpleDataServer {

  private final MainWindow mainWindow;

  public SimpleDataServer(MainWindow mainWindow) {
    this.mainWindow = mainWindow;
  }

  public void serverStart(String dataFile, int port) throws IOException {
    // 1. 创建并绑定服务器
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

    // 2. 注册 /data 路径的处理器
    server.createContext("/data", new DataHandler(dataFile));

    // 3. 启动服务器
    server.start();
    mainWindow.appendLog("[INFO] Java HTTP 服务器已启动");
    mainWindow.appendLog("[INFO] 访问地址：http://localhost:" + port + "/data");
    mainWindow.appendLog("[INFO] 读取文件：" + new File(dataFile).getAbsolutePath());
  }

  static class DataHandler implements HttpHandler {
    private final String dataFile;

    DataHandler(String dataFile) {
      this.dataFile = dataFile;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
      File file = new File(dataFile);

      // 404：文件不存在
      if (!file.exists()) {
        String msg = "❌ " + dataFile + " 文件不存在，请放在项目根目录！";
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(404, msg.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(msg.getBytes());
        }
        return;
      }

      // 200：返回文件内容（纯文本，每行一个数字）
      String content = Files.readString(file.toPath());
      exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
      exchange.sendResponseHeaders(200, content.getBytes().length);

      try (OutputStream os = exchange.getResponseBody()) {
        os.write(content.getBytes());
      }
    }
  }
}
