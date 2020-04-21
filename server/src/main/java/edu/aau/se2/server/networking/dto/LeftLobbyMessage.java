package edu.aau.se2.server.networking.dto;

public class LeftLobbyMessage extends BaseMessage {
    private boolean wasClosed;

    public LeftLobbyMessage() {
        this(false);
    }

    public LeftLobbyMessage(boolean wasClosed) {
        this.wasClosed = wasClosed;
    }

    public boolean isWasClosed() {
        return wasClosed;
    }

    public void setWasClosed(boolean wasClosed) {
        this.wasClosed = wasClosed;
    }
}
