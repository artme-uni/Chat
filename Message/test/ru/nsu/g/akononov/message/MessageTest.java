package ru.nsu.g.akononov.message;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

public class MessageTest {

    @Test
    public void testToStringResponse() {
        Message message = new Message(MessageType.TEXT_RESPONSE, "Hello!");
        String name = "Artem";
        message.setName(name);

        Date time = new Date();
        message.setTime(time);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        assertEquals(" ["+ dateFormat.format(time) +"] " + name + " : Hello!", message.toString());
    }

    @Test
    public void testToStringOther() {
        Message message = new Message(MessageType.SUCCESS_LOGIN, "Welcome!");
        String name = "Artem";
        message.setName(name);
        message.setTime(new Date());

        assertEquals("Welcome!", message.toString());
    }
}