package com.geekbrains.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainApp {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("Сервер запущен. Ожидаем подключения клиентов...");
            Socket socket = serverSocket.accept();
            System.out.println("Клиент подключен");

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            while (true) {
                String msg = in.readUTF();
                System.out.println("Сообщение клиента : " + msg);
                out.writeUTF("echo : " + msg);

                if (msg.equals("/end")) {
                    socket.close();
                    serverSocket.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}