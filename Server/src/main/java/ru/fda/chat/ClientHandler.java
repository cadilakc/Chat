package ru.fda.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private String username;
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public String getUsername() {
        return username;
    }

    public ClientHandler(Server server, Socket socket) throws Exception {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/login ")) {
                        String usernameFromLogin = msg.split("\\s")[1];
                        if (server.isUserOnline(usernameFromLogin)) {
                            sendMessage("/login_failed Current nickname is already used");
                            continue;
                        }
                        username = usernameFromLogin;
                        sendMessage("/login_ok " + username);
                        server.subscribe(this);
                        break;
                    }
                }
                while (true) {
                    String msg = in.readUTF();
                    server.broadcastMessage(username + ": " + msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    public void sendMessage(String message) throws IOException {
        out.writeUTF(message);
    }

    public void disconnect() {
        server.unsubscribe(this);
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
//        int msgCounter = 0;
//        while (true){
//            String msg = in.readUTF();
//            System.out.println(msg);
//
//            if(msg.startsWith("/")){
//                if(msg.equals("/stat")){
//                    out.writeUTF("Message count: " + msgCounter);
//                    continue;
//                }
//            }
//            out.writeUTF("ECHO: " + msg);
//            msgCounter++;
//        }
}
