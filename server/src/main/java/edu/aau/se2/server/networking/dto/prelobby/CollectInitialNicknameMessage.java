package edu.aau.se2.server.networking.dto.prelobby;

import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.BaseMessage;

public class CollectInitialNicknameMessage extends BaseMessage {
    private Player player;
    private String nickname;

    public CollectInitialNicknameMessage() {
    }

    public CollectInitialNicknameMessage(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getNickname(){ return nickname; }

    public void setNickname(String nicknameaaa){ this.nickname = nickname; }

    @Override
    public String toString() {
        return "ConnectedMessage{" +
                "player=" + player +
                '}';
    }
}
