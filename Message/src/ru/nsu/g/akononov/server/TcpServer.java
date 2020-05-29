package ru.nsu.g.akononov.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.nsu.g.akononov.message.*;

public class TcpServer implements Runnable {
    private static final Logger logger = Logger.getLogger(TcpServer.class.getName());

    private final Deque<Message> lastMessages = new LinkedList<>();
    private final List<SocketRequest> members = Collections.synchronizedList(new LinkedList<>());
    public final ServerSocket serverSocket;
    private boolean isJSON;

    public TcpServer(int port, boolean isJSON) throws IOException {
            serverSocket = new ServerSocket(port);
            this.isJSON = isJSON;
    }

    public boolean addMember(SocketRequest newClient, String name) {
        if (newClient != null) {
            for (SocketRequest requestProcessor : members) {
                if (requestProcessor.getName().equals(name)) {
                    return false;
                }
            }
            members.add(newClient);
            sendMessageToAll(new Message(MessageType.TEXT_RESPONSE_SYSTEM, name +
                    " has joined the chat"));
            logger.log(Level.INFO, name + " has joined");
            return true;
        }

        logger.log(Level.WARNING, "Null pointer Client");
        throw new NullPointerException();
    }

    public void removeMember(SocketRequest client) {
        if (client != null) {
            members.remove(client);
            sendMessageToAll(new Message(MessageType.TEXT_RESPONSE_SYSTEM, client.getName() +
                    " has left the chat"));
            logger.log(Level.INFO, client.getName() + " has left");
        }
    }

    public void sendMessageToAll(Message message) {
        for (SocketRequest client : members) {
            try {
                client.sendMessage(message);
                if (message.getType() == MessageType.TEXT_RESPONSE)
                    logger.log(Level.INFO, client.getName() + " has sent " + message.getText());
            } catch (IOException e) {
                removeMember(client);
                logger.log(Level.WARNING, client.getName() + " has just couldn't send the message");
            }
        }
    }

    public String getUsers() {
        StringBuilder stringBuilder = new StringBuilder();
        int index = 0;

        stringBuilder.append("Users list:");
        for (SocketRequest client : members) {
            index++;
            stringBuilder.append("\n").append(index).append(" : ").append(client.getName());
        }
        return stringBuilder.toString();
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();

                SocketRequest newRequest = new SocketRequest(lastMessages, socket, this, isJSON);
                Thread member = new Thread(newRequest);
                member.start();
            }
        } catch (IOException ioException) {
            close();
        }
    }

    public void close() {
        for (var member : members) {
            try {
                member.socket.close();
            } catch (IOException ioException) {
                logger.log(Level.WARNING, "Cannot close the socket");
            }
        }
    }
}