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
    private boolean isJSON;

    ObjectOutputStream out;

    private boolean isLogin = false;
    private boolean exit = false;

    public TcpClient(String ip, int port, boolean isJSON) {
        this.isJSON = isJSON;
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
        Socket socket;

        try {
            socket = new Socket(ip, port);
        } catch (IOException e) {
            updateObservers(OutputType.ERROR, "Server isn't working now." + "\nTry to reconnect later");
            logger.log(Level.WARNING, "Server isn't working");
            exit = true;
            return;
        }


        try (ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {

            out = outputStream;

            while (!exit) {
                Message newMessage = Message.deserialization(inputStream, isJSON);

                if (newMessage.getType() == MessageType.SUCCESS_LOGIN) {
                    isLogin = true;
                }
                if (newMessage.getType() == MessageType.EXIT) {
                    isLogin = false;
                    exit = true;
                }
                updateObservers(getObserverType(newMessage), newMessage.toString());
            }

        } catch (IOException e) {
            updateObservers(OutputType.ERROR, "Sorry, you are disconnected!");
        } catch (ClassNotFoundException e) {
            logger.log(Level.WARNING, "Object Input Stream error", e);
        }

        try {
            socket.close();
        } catch (IOException ioException) {
            logger.log(Level.WARNING, "Cannot close socket", ioException);
        }
    }


    public void sendMessage(MessageType type, String text) {
        if (!exit) {
            try {
                new Message(type, text).serialization(out, isJSON);
            } catch (IOException e) {
                logger.log(Level.WARNING, Thread.currentThread().getName() + " can't send text");
            }
        }
    }

    public void setIsLogin(boolean isLogin) {
        this.isLogin = isLogin;
    }

    public OutputType getObserverType(Message message) {
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


    public MessageType parseMessage(String string) {
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
