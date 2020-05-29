package edu.aau.se2.server.networking.dto.prelobby;

import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.BaseMessage;

public class ChangeNicknameMessage extends BaseMessage {
    private Player player;
    private String nickname;
    private int fromPlayerID;

    public ChangeNicknameMessage() {
    }

    public ChangeNicknameMessage(Player player) {
        this.player = player;
    }

    public ChangeNicknameMessage(int fromPlayerID, String nickname) {
        this.fromPlayerID = fromPlayerID;
        this.nickname = nickname;
        //player.setNickname(nickname);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getNickname(){ return nickname; }

    public void setNickname(String nickname){ this.nickname = nickname; }

    public int getFromPlayerID() {
        return fromPlayerID;
    }

    public void setFromPlayerID(int fromPlayerID) {
        this.fromPlayerID = fromPlayerID;
    }

    @Override
    public String toString() {
        return "ConnectedMessage{" +
                "player=" + player +
                '}';
    }
}
