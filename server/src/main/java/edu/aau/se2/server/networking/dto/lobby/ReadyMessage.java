package edu.aau.se2.server.networking.dto.lobby;

import edu.aau.se2.server.networking.dto.InLobbyMessage;

public class ReadyMessage extends InLobbyMessage {
    private boolean ready = true;

    public ReadyMessage(int lobbyID, int fromPlayerID, boolean ready) {
        super(lobbyID, fromPlayerID);
        this.ready = ready;
    }

    public ReadyMessage() {
        super();
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    @Override
    public String toString() {
        return "ReadyMessage{" +
                "ready=" + ready +
                ", lobbyID=" + lobbyID +
                ", fromPlayerID=" + fromPlayerID +
                '}';
    }
}
