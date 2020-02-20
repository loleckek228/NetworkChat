package com.geekbrains.chat.client;

import java.io.*;

public class ClientHistory {
    private FileOutputStream history;

    public ClientHistory(String fileName) throws FileNotFoundException {
        history = new FileOutputStream(fileName);
    }

    public void safe(String input) throws IOException {
        history.write(input.getBytes());
    }
}
