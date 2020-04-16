package edu.aau.se2.server.networking.dto;

import edu.aau.se2.server.data.Player;

public class ConnectedMessage extends BaseMessage {
    private Player player;

    public ConnectedMessage() {
    }

    public ConnectedMessage(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
