package com.geekbrains.chat.server;

public interface AuthManager {
    String getNicknameByLoginAndPassword(String login, String password);
    void changeNicname(String login, String newNickName);
    void connect();
    void disconnect();
}
