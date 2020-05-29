package ru.nsu.g.akononov.message;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message implements Serializable {
    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private String name;
    private MessageType type;
    private String text;
    private String time;

    public Message(MessageType type, String text) {
        this.type = type;
        this.text = text;
    }

    public Message() {
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTime(Date time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        this.time = dateFormat.format(time);
    }

    public void serialization(ObjectOutputStream output, boolean isJSON) throws IOException {
        if (isJSON) {
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] msg = objectMapper.writeValueAsBytes(this);

            output.writeInt(msg.length);
            output.write(msg);
            output.flush();
            return;
        }
        output.writeObject(this);
        output.flush();
    }

    public static Message deserialization(ObjectInputStream input, boolean isJSON) throws IOException, ClassNotFoundException {
        if (isJSON) {
            ObjectMapper objectMapper = new ObjectMapper();
            int size = input.readInt();
            byte[] message = new byte[size];
            input.read(message, 0, size);
            return objectMapper.readValue(message, Message.class);
        }
        return (Message) input.readObject();
    }


    @Override
    public String toString() {
        if (type == MessageType.TEXT_RESPONSE) {
            return " [" + time + "] " + name + " : " + text;
        }
        return getText();
    }
}
