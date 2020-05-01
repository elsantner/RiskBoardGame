package edu.aau.se2.server.networking.dto;

public class ErrorMessage extends BaseMessage {
    public static final int JOIN_LOBBY_FULL = -1;        // Lobby to join was already full
    public static final int JOIN_LOBBY_CLOSED = -2;      // Lobby to join was closed / did not exist (anymore)
    public static final int JOIN_LOBBY_ALREADY_JOINED = -3;      // Lobby to join was closed / did not exist (anymore)
    public static final int JOIN_LOBBY_UNKNOWN = -4;     // Unknown error while joining lobby

    private int errorCode;

    public ErrorMessage() {

    }

    public ErrorMessage(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
