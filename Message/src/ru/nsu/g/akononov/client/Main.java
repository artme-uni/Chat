package ru.nsu.g.akononov.client;

import ru.nsu.g.akononov.client.mvc.controller.Controller;
import ru.nsu.g.akononov.client.mvc.model.TcpClient;

import java.io.IOException;
import java.util.logging.LogManager;

public class Main {
    final private static int PORT = 18201;

    public static void main(String[] args) throws IOException {
        if(args.length > 1)
            System.out.println("Expected input: ./client [-json]");

        boolean isJSON = false;
        if(args.length == 1 && args[0].toLowerCase().equals("-json"))
            isJSON = true;

        LogManager logManager = LogManager.getLogManager();
        logManager.readConfiguration(Main.class.getResourceAsStream("/ru/nsu/g/akononov/client/resources/logging.properties"));

        TcpClient tcpClient = new TcpClient("127.0.0.1", PORT, isJSON);
        Controller controller = new Controller(tcpClient);
        Thread thread = new Thread(tcpClient);
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
