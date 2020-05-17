package edu.aau.se2.server.networking.dto.prelobby;

import edu.aau.se2.server.networking.dto.BaseMessage;

public class RequestLobbyListMessage extends BaseMessage {
    private int fromPlayerID;

    public RequestLobbyListMessage() {
    }

    public RequestLobbyListMessage(int fromPlayerID) {
        this.fromPlayerID = fromPlayerID;
    }

    public int getFromPlayerID() {
        return fromPlayerID;
    }

    public void setFromPlayerID(int fromPlayerID) {
        this.fromPlayerID = fromPlayerID;
    }
}
