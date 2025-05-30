package com.example.server.network;

import com.example.client.utils.ServerConfig;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Getter
@Setter
public class ServerClient {
    private static ServerClient instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean connected = false;
    private String ip;
    private int port;

    private ServerClient() {
        // Не подключаемся сразу в конструкторе
    }

    public static synchronized ServerClient getInstance() {
        if (instance == null) {
            instance = new ServerClient();
        }
        return instance;
    }

    public void connect() throws IOException {
        try {
            String serverAddress = ServerConfig.getIp();
            int serverPort = ServerConfig.getPort();
            ip = serverAddress;
            port = serverPort;
            socket = new Socket(serverAddress, serverPort);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;
            System.out.println("Подключение к серверу " + serverAddress + ":" + serverPort + " установлено.");

        } catch (IOException e) {
            System.err.println("Не удалось подключиться к серверу " + ip + ":" + port + ": " + e.getMessage());
            connected = false;
        }
    }

    public boolean connect(String ip, int port) {
        try {
            this.ip = ip;
            this.port = port;
            socket = new Socket(ip, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;
            System.out.println("Подключение к серверу " + ip + ":" + port + " установлено.");


            return true;
        } catch (IOException e) {
            System.err.println("Не удалось подключиться к серверу " + ip + ":" + port + ": " + e.getMessage());
            connected = false;
            return false;
        }
    }

    public boolean checkConnection(String ip, int port) {
        try {
            if (connected && (!this.ip.equals(ip) || this.port != port)) {
                disconnect();
            }
            if (!connected) {
                return connect(ip, port);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка проверки соединения: " + e.getMessage());
            connected = false;
            return false;
        }
    }

    public Response sendRequest(Request request) {
        if (!connected) {
            System.err.println("Нет активного соединения с сервером.");
            return new Response(false, "Нет соединения с сервером", null);
        }
        try {
            out.writeObject(request);
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка при отправке запроса: " + e.getMessage());
            return new Response(false, "Ошибка связи с сервером: " + e.getMessage(), null);
        }
    }

    public void disconnect() {
        try {

            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
            connected = false;
            System.out.println("Отключение от сервера выполнено.");
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
        }
    }
}