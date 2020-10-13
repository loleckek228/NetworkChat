package com.geekbrains.chat.server;

import java.sql.*;

public class DBAuthManager implements AuthManager {
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement getNickname;
    private static PreparedStatement changeNickname;

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            stmt = connection.createStatement();
            getNickname = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? AND password = ?;");
            changeNickname = connection.prepareStatement("UPDATE users SET nickname = ? WHERE nickname = ?;");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (getNickname != null) {
                getNickname.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (changeNickname != null) {
                changeNickname.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTableEx() throws SQLException {
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (login TEXT, password TEXT, nickname TEXT);");
    }

    private static void insertEx(String login, String password, String user) throws SQLException {
        connection.setAutoCommit(false);
        changeNickname.setString(1, login);
        changeNickname.setString(2, password);
        changeNickname.setString(3, user);
        changeNickname.executeUpdate();
        connection.commit();
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        try {
            getNickname.setString(1, login);
            getNickname.setString(2, password);
            try (ResultSet rs = getNickname.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void changeNicname(String oldNickName, String newNickName) {
        try {
            changeNickname.setString(1, newNickName);
            changeNickname.setString(2, oldNickName);
            changeNickname.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}