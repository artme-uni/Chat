package ru.nsu.g.akononov.client.mvc.controller;
import ru.nsu.g.akononov.message.*;
import ru.nsu.g.akononov.client.mvc.model.TcpClient;
import ru.nsu.g.akononov.client.mvc.view.View;

public class Controller {
    private final TcpClient model;

    public Controller(TcpClient model) {
        this.model = model;
        View view = new View(this.model, this);
    }

    public void sendMessage(String name) {
        MessageType type=model.parseMessage(name);
        model.sendMessage(type,name);
    }
}
