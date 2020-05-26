package ru.nsu.g.akononov.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.nsu.g.akononov.message.*;

public class TcpServer implements Runnable {
    private static Logger logger = Logger.getLogger(TcpServer.class.getName());

    private List<RequestProcessor> clients = Collections.synchronizedList(new LinkedList<>());
    private final int port;
    private ServerSocket serverSocket;

    public TcpServer(int port) {
        this.port = port;
    }


    public void close() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Couldn't close server socket");
            }
        }

        for (RequestProcessor client : clients) {
            try {
                client.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Couldn't close " + client.getName() + "'s socket");
            }
        }
    }

    public boolean addClient(RequestProcessor newClient, String name) {
        if (newClient != null) {
            for (RequestProcessor requestProcessor : clients) {
                if (requestProcessor.getName().equals(name)) {
                    return false;
                }
            }
            clients.add(newClient);
            sendMessageAllClients(new Message(MessageType.TEXT_RESPONSE_SYSTEM, name +
                    " has joined the chat\n"));
            return true;
        }
        logger.log(Level.WARNING, "Null pointer Client");
        throw new NullPointerException();
    }

    public void removeClient(RequestProcessor client) {
        if (client != null) {
            clients.remove(client);
            sendMessageAllClients(new Message(MessageType.TEXT_RESPONSE_SYSTEM, client.getName() +
                    " has just left the chat\n"));
            logger.log(Level.INFO, client.getName() + " has just removed");
        }
    }

    public void sendMessageAllClients(Message message) {
        for (RequestProcessor client : clients) {
            try {
                client.sendMessage(message);
            } catch (IOException e) {
                removeClient(client);
                logger.log(Level.WARNING, client.getName() + " has just couldn't send the message");
            }
        }
    }

    public String getUsers() {
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;

        for (RequestProcessor client : clients) {
            count++;
            stringBuilder.append("№" + count + ":" + client.getName() + "\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                logger.log(Level.INFO, "Connection has received from " + socket.getInetAddress() +
                        ":" + socket.getPort());
                RequestProcessor newRequest = new RequestProcessor(socket, this);
                Thread client = new Thread(newRequest);
                client.start();
                logger.log(Level.INFO, "Start RequestProcessor");
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Server couldn't create socket");
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "", e);
                }
            }
        }
    }
}
