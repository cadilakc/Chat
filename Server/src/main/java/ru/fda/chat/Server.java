package ru.fda.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients;

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("Сервер запущен на порту: " + port);
            while (true) {
                System.out.println("Ожидаю подключения клиента...");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clients) {
            try {
                clientHandler.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendPrivateMessage(ClientHandler sender, String receiverUsername, String message) throws IOException {
        for (ClientHandler c : clients) {
            if (c.getUsername().equals(receiverUsername)) {
                c.sendMessage("От: " + sender.getUsername() + " Сообщение: " + message);
                sender.sendMessage("Пользователю: " + receiverUsername + " Сообщение: " + message);
                return;
            }
        }
            sender.sendMessage("Невозможно отправить сообщение пользователю: " + receiverUsername + ". Такого пользователя нет в сети.");
    }

    public boolean isUserOnline(String username) {
        for (ClientHandler c : clients) {
            if (c.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
}
