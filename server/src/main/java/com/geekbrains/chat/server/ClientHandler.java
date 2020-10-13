package com.geekbrains.chat.server;

import org.apache.logging.log4j.Level;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/auth ")) {
                        server.getLOGGER().info("Авторизация клиента: "+ msg);
                        String[] tokens = msg.split(" ", 3);
                        String nickFromAuthManager = server.getAuthManager().getNicknameByLoginAndPassword(tokens[1], tokens[2]);
                        if (nickFromAuthManager != null) {
                            if (server.isNickBusy(nickFromAuthManager)) {
                                sendMsg("Данный пользователь уже в чате");
                                continue;
                            }
                            nickname = nickFromAuthManager;
                            sendMsg("/authok " + nickname);
                            server.subscribe(this);
                            sendMsg(server.getChatHistory().showHistory());
                            break;
                        } else {
                            sendMsg("Указан неверный логин/пароль");
                        }
                    }
                }

                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/")) {
                        server.getLOGGER().info("Команда от клиента " + nickname + ": " + msg);
                        if (msg.startsWith("/w ")) {
                            String[] tokens = msg.split(" ", 3);
                            server.sendPrivateMsg(this, tokens[1], tokens[2]);
                            continue;
                        }
                        if (msg.startsWith("/change_nick ")) {
                            String[] tokens = msg.split(" ");
                            server.getAuthManager().changeNicname(nickname, tokens[1]);
                            nickname = tokens[1];
                            sendMsg("/change_nick_confirm " + nickname);
                            sendMsg("Вы теперь в сети как: " + nickname);
                            server.broadcastClientsList();
                            continue;
                        }
                        if (msg.equals("/end")) {
                            sendMsg("/end_confirm");
                            break;
                        }

                    } else {
                        server.getLOGGER().info("Сообщение от клиента "  + nickname + ": " + msg);
                        server.broadcastMsg(nickname + ": " + msg, true);
                    }
                }
            } catch (IOException e) {
                server.getLOGGER().error(Level.ERROR, e);
            } finally {
                try {
                    close();
                } catch (IOException e) {
                    server.getLOGGER().error(Level.ERROR, e);
                }
            }
        });

        executorService.shutdown();
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            server.getLOGGER().error(Level.ERROR, e);
        }
    }

    public void close() throws IOException {
        server.unsubscribe(this);
        nickname = null;

        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                server.getLOGGER().error(Level.ERROR, e);
            }
        }

        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                server.getLOGGER().error(Level.ERROR, e);
            }
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                server.getLOGGER().error(Level.ERROR, e);
            }
        }
    }
}