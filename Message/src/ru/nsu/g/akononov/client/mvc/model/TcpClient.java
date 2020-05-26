package ru.nsu.g.akononov.client.mvc.model;

import ru.nsu.g.akononov.message.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TcpClient extends Observable implements Runnable {

    private final Logger logger = Logger.getLogger(TcpClient.class.getName());
    private Properties property;

    private final String ip;
    private final int port;
    private Socket socket;

    ObjectOutputStream out;

    private boolean isLogin = false;
    private boolean exit = false;

    public TcpClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
        loadCommands();
    }

    private void loadCommands() {

        InputStream commands = null;
        try {
            commands = getClass().getClassLoader().getResourceAsStream("ru/nsu/g/akononov/client/resources/commands.properties");
            if (commands == null)
                throw new IOException();
            property = new Properties();
            property.load(commands);

        } catch (IOException e) {
            logger.log(Level.WARNING, "Cannot load commands from commands.properties");
        } finally {
            try {
                if (commands != null) {
                    commands.close();
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, "The program couldn't close InputStream");
            }
        }
    }

    @Override
    public void run() {
        try {
            socket = new Socket(ip, port);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Server isn't working");
            updateObservers(OutputType.ERROR, "Server isn't working now." + "\nTry again later");
        }

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {

            out = objectOutputStream;

            while (!exit) {
                Message newMessage = (Message) objectInputStream.readObject();

                if (newMessage.getType() == MessageType.SUCCESS_LOGIN) {
                    isLogin = true;
                }
                if (newMessage.getType() == MessageType.EXIT) {
                    isLogin = false;
                    exit = true;
                }

                updateObservers(ObserverType(newMessage), newMessage.getText());
            }

        } catch (IOException e) {
            logger.log(Level.WARNING, "Socket error", e);
            updateObservers(OutputType.ERROR, "Something go wrong with a server!");
        } catch (ClassNotFoundException e) {
            logger.log(Level.WARNING, "Object Input Stream error", e);
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ioException) {
                logger.log(Level.WARNING, "Cannot close socket", ioException);
            }
        }

    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
        } catch (IOException e) {
            logger.log(Level.WARNING, Thread.currentThread().getName() + " can't send text");
        }
    }

    public void sendMessage(MessageType type, String text) {
        sendMessage(new Message(type, text));
    }

    private OutputType ObserverType(Message message) {
        MessageType type = message.getType();

        switch (type) {
            case ERROR:
                return OutputType.ERROR;

            case TEXT_RESPONSE:
                return OutputType.SHARED;

            default:
                return OutputType.SYSTEM;
        }
    }


    public MessageType parseText(String string) {
        if (!isLogin) {
            return MessageType.LOGIN;
        }

        try {
            return MessageType.valueOf(property.getProperty(string));
        } catch (Exception e) {
            return MessageType.TEXT_REQUEST;
        }
    }
}
