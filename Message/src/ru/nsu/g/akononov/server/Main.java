package ru.nsu.g.akononov.server;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
    final private static int PORT = 18201;

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {
        if(args.length > 1)
            System.out.println("Expected input: ./client [-json]");

        boolean isJSON = false;
        if(args.length == 1 && args[0].toLowerCase().equals("-json"))
            isJSON = true;

        LogManager logManager = LogManager.getLogManager();
        logManager.readConfiguration(Main.class.getResourceAsStream("/ru/nsu/g/akononov/server/resources/logging.properties"));

        try {
            TcpServer server = new TcpServer(PORT, isJSON);
            Thread serverThread = new Thread(server);

            System.out.println("Server is ON");

            serverThread.start();
            waitCommand();
            server.serverSocket.close();
            server.close();
            serverThread.join();

            System.out.println("Server is OFF");
        } catch (InterruptedException ex) {
            logger.log(Level.WARNING, "Server is interrupted");
        }
    }


    private static void waitCommand() {
        System.out.println("Type 'OFF' to close server");
        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()) {
            String input = in.nextLine();

            if (input.toLowerCase().equals("off"))
                break;

            System.out.println("Unknown command!\nType 'OFF' to close server");
        }
    }
}
