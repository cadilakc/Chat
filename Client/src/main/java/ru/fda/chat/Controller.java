package ru.fda.chat;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Controller {
    @FXML
    TextArea textChat;

    @FXML
    TextField message, usernameField;

    @FXML
    HBox msgPanel, loginPanel;

    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private String username;

    public void setUsername(String username) {
        this.username = username;
        if (username != null) {
            loginPanel.setVisible(false);
            loginPanel.setManaged(false);
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);
        } else {
            loginPanel.setVisible(true);
            loginPanel.setManaged(true);
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);
        }
    }

    public void login() {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        if (usernameField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Имя пользователя не может быть пустым!", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        try {
            out.writeUTF("/login " + usernameField.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Thread t = new Thread(() -> {
                try {

                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/login_ok ")) {
                            setUsername(msg.split("\\s")[1]);
                            break;
                        }
                        if (msg.startsWith("/login_failed ")) {
                            String cause = msg.split("\\s", 2)[1];
                            textChat.appendText(cause + "\n");
                        }
                    }

                    while (true) {
                        String msg = in.readUTF();
                        textChat.appendText(msg + "\n");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    disconnect();
                }
            });
            t.start();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно подключиться", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void clickSend() {
        try {
            out.writeUTF(message.getText());
            message.clear();
            message.requestFocus();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно отправить сообщение", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void disconnect() {
        setUsername(null);
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
