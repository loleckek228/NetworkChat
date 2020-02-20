package com.geekbrains.chat.server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ChatHistory {

    private FileOutputStream history;

    public ChatHistory() throws FileNotFoundException {
        history = new FileOutputStream("chat_history.txt");
    }

    public void safe(String input) throws IOException {
        history.write(input.getBytes());
        history.write("\n".getBytes());
    }

    public String showHistory() {
        StringBuilder history = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader("chat_history.txt"))) {
            List<String> usersMessages = new ArrayList<>();
            usersMessages = Files.readAllLines(Paths.get("chat_history.txt"));
            int firstPosition = 0;

            if (usersMessages.size() > 100)
                firstPosition = usersMessages.size() - 100;

            for (int i = firstPosition; i < usersMessages.size(); i++) {
                history.append(usersMessages.get(i)).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return history.toString();
    }
}
