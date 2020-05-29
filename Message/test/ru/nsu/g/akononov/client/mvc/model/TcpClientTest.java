package ru.nsu.g.akononov.client.mvc.model;

import org.junit.Test;
import ru.nsu.g.akononov.message.Message;
import ru.nsu.g.akononov.message.MessageType;

import static org.junit.Assert.*;

public class TcpClientTest {

    @Test
    public void parseMessageLogin() {
        TcpClient client = new TcpClient("", 0, false);
        MessageType type = client.parseMessage("/exit");
        assertEquals(MessageType.LOGIN, type);
    }

    @Test
    public void parseMessageExit() {
        TcpClient client = new TcpClient("", 0,false);
        client.setIsLogin(true);
        MessageType type = client.parseMessage("/exit");
        assertEquals(MessageType.EXIT, type);
    }

    @Test
    public void parseMessageUsers() {
        TcpClient client = new TcpClient("", 0,false);
        client.setIsLogin(true);
        MessageType type = client.parseMessage("/users");
        assertEquals(MessageType.GET_USERS, type);
    }

    @Test
    public void parseMessageRequest() {
        TcpClient client = new TcpClient("", 0,false);
        client.setIsLogin(true);
        MessageType type = client.parseMessage("Hello!");
        assertEquals(MessageType.TEXT_REQUEST, type);
    }
}