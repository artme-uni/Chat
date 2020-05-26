package ru.nsu.g.akononov.client.mvc.model;

public interface Observer {
    void updateView(OutputType type, String text);
}
