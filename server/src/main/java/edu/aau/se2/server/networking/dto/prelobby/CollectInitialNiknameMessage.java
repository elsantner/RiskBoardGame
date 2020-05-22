package edu.aau.se2.server.networking.dto.prelobby;

import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.BaseMessage;

public class CollectInitialNiknameMessage extends BaseMessage {
    private Player player;

    public CollectInitialNiknameMessage() {
    }

    public CollectInitialNiknameMessage(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public String toString() {
        return "ConnectedMessage{" +
                "player=" + player +
                '}';
    }
}
