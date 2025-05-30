import com.example.server.network.ClientThread;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class StartSMSServer {

    private static final AtomicInteger clientCount = new AtomicInteger(0);

    private static void startServer() {
        ResourceBundle bundle = ResourceBundle.getBundle("server");
        int serverPort = Integer.parseInt(bundle.getString("SERVER_PORT"));

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Сервер запущен на порту " + serverPort + "!");
            while (true) {
                Socket clientAccepted = serverSocket.accept();
                // Увеличиваем счётчик
                int currentCount = clientCount.incrementAndGet();
                System.out.println("Соединение установлено. Количество клиентов: " + currentCount);

                // Передаём клиентский сокет в поток обработки
                executorService.submit(() -> new ClientThread(clientAccepted, clientCount).run());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        startServer();
    }
}

