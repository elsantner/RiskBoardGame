package edu.aau.se2.server.networking.dto;

public class TextMessage extends BaseMessage {

    public TextMessage() {
    }

    public TextMessage(String text) {
        this.text = text;
    }

    public String text;

    @Override
    public String toString() {
        return String.format("TextMessage: %s", text);
    }
}
