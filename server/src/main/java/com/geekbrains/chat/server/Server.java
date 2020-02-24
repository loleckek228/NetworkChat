package com.geekbrains.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private AuthManager authManager;
    private List<ClientHandler> clients;
    private final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public ChatHistory getChatHistory() {
        return chatHistory;
    }

    private ChatHistory chatHistory;

    public AuthManager getAuthManager() {
        return authManager;
    }

    public Server(int port) {
        clients = new ArrayList<>();
        authManager = new DBAuthManager();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен. Ожидаем подключения клиентов...");
            authManager.connect();
            chatHistory = new ChatHistory();
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            authManager.disconnect();
        }
    }

    public void broadcastMsg(String msg, boolean isDateTime) throws IOException {

        if (isDateTime) {
            msg = String.format("[%s] %s", LocalDateTime.now().format(DTF), msg);
            chatHistory.safe(msg);
        }

        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }
    }

    public void broadcastClientsList() throws IOException {
        StringBuilder stringBuilder = new StringBuilder("/clients_list ");

        for (ClientHandler o : clients) {
            stringBuilder.append(o.getNickname()).append(" ");
        }

        stringBuilder.setLength(stringBuilder.length() - 1);
        String out = stringBuilder.toString();
        broadcastMsg(out, false);
    }

    public boolean isNickBusy(String nickname) {
        for (ClientHandler o : clients) {
            if (o.getNickname().equals(nickname)) {
                return true;
            }
        }

        return false;
    }

    public void sendPrivateMsg(ClientHandler sender, String recipient, String msg) {
        if (sender.getNickname().equals(recipient)) {
            sender.sendMsg("Нельзя отправить сообщение самому себе");
            return;
        }
        for (ClientHandler o : clients) {
            if (recipient.equals(o.getNickname())) {
                o.sendMsg("from " + sender.getNickname() + ": " + msg);
                sender.sendMsg("to " + recipient + ": " + msg);
                return;
            }
        }
        sender.sendMsg(recipient + " не в сети, либо такого пользователя не существует");
    }

    public synchronized void subscribe(ClientHandler clientHandler) throws IOException {
        broadcastMsg(clientHandler.getNickname() + " в сети", false);
        clients.add(clientHandler);
        broadcastClientsList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) throws IOException {
        broadcastMsg(clientHandler.getNickname() + " не сети", false);
        clients.remove(clientHandler);
        broadcastClientsList();
    }
}