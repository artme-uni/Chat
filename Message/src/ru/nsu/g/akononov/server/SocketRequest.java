package ru.nsu.g.akononov.server;

import ru.nsu.g.akononov.message.*;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketRequest implements Runnable {
    private final Logger logger = Logger.getLogger(SocketRequest.class.getName());
    private final Deque<Message> lastMessages;
    private final int savedMessagesCount = 30;

    private final TcpServer tcpServer;
    public final Socket socket;
    private String name;
    private ObjectOutputStream out;
    private boolean exit = false;
    private boolean isJSON;

    public SocketRequest(Deque<Message> lastMessages, Socket socket, TcpServer tcpServer, boolean isJSON) {
        this.lastMessages = lastMessages;
        this.socket = socket;
        this.tcpServer = tcpServer;
        this.isJSON = isJSON;
    }

    public void sendMessage(Message message) throws IOException {
        if (out != null) {
            message.serialization(out, isJSON);
        }
    }

    private void saveMessage(Message message) {
        synchronized (lastMessages) {
            lastMessages.addLast(message);
            if (lastMessages.size() > savedMessagesCount) {
                lastMessages.removeFirst();
            }
        }
    }

    @Override
    public void run() {

        try (ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
            out = output;

            while (!exit) {
                Message message = Message.deserialization(input, isJSON);
                if (message.getType() == MessageType.TEXT_REQUEST)
                    saveMessage(message);
                response(message);
            }
        } catch (IOException e) {
            try {
                if (!socket.isClosed())
                    socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            logger.log(Level.WARNING, "Cannot load class Message", e);
        } finally {
            tcpServer.removeMember(this);
        }
    }

    public String getName() {
        return name;
    }

    private void response(Message message) throws IOException {
        switch (message.getType()) {
            case LOGIN: {
                if (!tcpServer.addMember(this, message.getText())) {
                    sendMessage(new Message(MessageType.ERROR, "This name already in use!"));
                } else {
                    this.name = message.getText();
                    sendMessage(new Message(MessageType.SUCCESS_LOGIN, "Welcome!"));
                    for (var msg : lastMessages) {
                        sendMessage(msg);
                    }
                }
                break;
            }
            case GET_USERS: {
                Message users = new Message(MessageType.GET_USERS, tcpServer.getUsers());
                sendMessage(users);
                logger.log(Level.INFO, "Server has sent users list to " + name);
                break;
            }
            case EXIT: {
                Message exitMessage = new Message(MessageType.GET_USERS, "You've left the chat");
                sendMessage(exitMessage);
                exit = true;
                logger.log(Level.INFO, name + " has left the chat");
                break;
            }
            case TEXT_REQUEST: {
                message.setType(MessageType.TEXT_RESPONSE);
                message.setTime(new Date());
                message.setName(name);

                tcpServer.sendMessageToAll(message);
                break;
            }
        }
    }

    public void close() {
        exit = true;
    }
}
