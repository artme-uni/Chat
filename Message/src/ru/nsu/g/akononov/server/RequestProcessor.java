package ru.nsu.g.akononov.server;

import ru.nsu.g.akononov.message.*;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestProcessor implements Runnable {
    private final Logger logger = Logger.getLogger(RequestProcessor.class.getName());

    private final TcpServer tcpServer;
    private final Socket socket;
    private String name;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private boolean exit = false;

    public RequestProcessor(Socket socket, TcpServer tcpServer) {
        this.socket = socket;
        this.tcpServer = tcpServer;
    }

    public void close() throws IOException {
        socket.close();
    }

    public void sendMessage(Message message) throws IOException {
        if (objectOutputStream != null) {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
        }
    }

    @Override
    public void run() {

        try {
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            objectInputStream = new ObjectInputStream(input);
            objectOutputStream = new ObjectOutputStream(output);

            while (!exit) {
                logger.log(Level.INFO, Thread.currentThread().getName() + " is waiting message...");
                Message message = (Message) objectInputStream.readObject();

                logger.log(Level.INFO, Thread.currentThread().getName() + " received: type=" + message.getType() +
                        " text=" + message.getText());
                clientsResponse(message);
            }

        } catch (IOException e) {
            logger.log(Level.WARNING, "Sockets(Maybe client has just closed the app):", e);

        } catch (ClassNotFoundException e) {
            logger.log(Level.WARNING, "Read object error", e);
        } finally {
            if (!exit)
                tcpServer.removeClient(this);
            try {
                if (objectOutputStream != null) {
                    objectOutputStream.close();
                }
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Object Streams has just closed error", ex);
            }
            logger.log(Level.INFO, Thread.currentThread() + " has just ended work");
        }
    }

    public String getName() {
        return name;
    }

    private void clientsResponse(Message message) throws IOException {
        switch (message.getType()) {
            case LOGIN: {
                if (!tcpServer.addClient(this, message.getText())) {
                    Message badName = new Message(MessageType.ERROR, "This name has already used by someone. " +
                            "Try to choose another name:");
                    sendMessage(badName);
                } else {
                    this.name = message.getText();
                    Message goodName = new Message(MessageType.SUCCESS_LOGIN, "Welcome to the chat room," +
                            name + "!\n");
                    sendMessage(goodName);

                }
                break;
            }
            case GET_USERS: {
                Message users = new Message(MessageType.GET_USERS, tcpServer.getUsers());
                sendMessage(users);
                logger.log(Level.INFO, "Server has just sent all users to " + name);
                break;
            }
            case EXIT: {
                Message exitMessage = new Message(MessageType.GET_USERS, "You've just left the chat room.\n");
                sendMessage(exitMessage);
                tcpServer.removeClient(this);
                exit = true;
                logger.log(Level.INFO, name + " has just left the chat room");
                break;
            }
            case TEXT_REQUEST: {

                message.setType(MessageType.TEXT_RESPONSE);
                message.setTime(new Date());
                message.setText(name + " [" + message.getTime().toString() + "]" + ":" + message.getText());
                tcpServer.sendMessageAllClients(message);
                logger.log(Level.INFO, Thread.currentThread().getName() + " has just sent the message to " +
                        name);
                break;
            }
        }
    }


}
