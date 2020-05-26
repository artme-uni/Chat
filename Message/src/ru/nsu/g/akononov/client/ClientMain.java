package ru.nsu.g.akononov.client;

import ru.nsu.g.akononov.client.mvc.controller.Controller;
import ru.nsu.g.akononov.client.mvc.model.TcpClient;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ClientMain {
    final private static int PORT = 18201;

    private static final Logger logger = Logger.getLogger(ClientMain.class.getName());

    public static void main(String[] args) throws IOException {
        LogManager logManager = LogManager.getLogManager();
        logManager.readConfiguration(ClientMain.class.getResourceAsStream("/ru/nsu/g/akononov/client/resources/logging.properties"));

        TcpClient tcpClient = new TcpClient("127.0.0.1", PORT);
        Controller controller = new Controller(tcpClient);
        Thread thread = new Thread(tcpClient);
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Client has interrupted", e);
        }

        logger.log(Level.INFO, "Client app has closed");
    }
}
