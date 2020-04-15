package edu.aau.se2.server.networking.dto;

public class TextMessage implements BaseMessage {

    public TextMessage() {
    }

    public TextMessage(String text) {
        this.text = text;
    }


    private String text;

    @Override
    public String toString() {
        return String.format("TextMessage: %s", text);
    }

    public String getText() {
        return text;
    }

}
