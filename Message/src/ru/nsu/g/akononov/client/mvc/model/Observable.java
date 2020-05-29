package ru.nsu.g.akononov.client.mvc.model;

import java.util.ArrayList;

public class Observable {
    private final ArrayList<Observer> observers = new ArrayList<>();

    public void registerObserver(Observer observer) {
        if (observer == null) {
            throw new NullPointerException();
        }
        if (observers.contains(observer)) {
            throw new IllegalArgumentException("Repeated observer:" + observer);
        }
        observers.add(observer);
    }

    public synchronized void updateObservers(OutputType type, String text) {
        for (Observer observer : observers) {
            observer.updateView(type, text);
        }
    }
}
