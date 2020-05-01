package edu.aau.se2.server.networking.dto.prelobby;

import java.util.List;

import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.BaseMessage;

public class LobbyListMessage extends BaseMessage {

    private List<LobbyData> lobbies;

    public LobbyListMessage() {

    }

    public LobbyListMessage(List<LobbyData> lobbies) {
        this.lobbies = lobbies;
    }

    public List<LobbyData> getLobbies() {
        return lobbies;
    }

    public void setLobbies(List<LobbyData> lobbies) {
        this.lobbies = lobbies;
    }



    public static class LobbyData {
        private int lobbyID;
        private Player host;
        private int playerCount;

        public LobbyData() {

        }

        public LobbyData(int lobbyID, Player host, int playerCount) {
            this.lobbyID = lobbyID;
            this.host = host;
            this.playerCount = playerCount;
        }

        public int getLobbyID() {
            return lobbyID;
        }

        public void setLobbyID(int lobbyID) {
            this.lobbyID = lobbyID;
        }

        public Player getHost() {
            return host;
        }

        public void setHost(Player host) {
            this.host = host;
        }

        public int getPlayerCount() {
            return playerCount;
        }

        public void setPlayerCount(int playerCount) {
            this.playerCount = playerCount;
        }

        @Override
        public String toString() {
            return "LobbyData{" +
                    "lobbyID=" + lobbyID +
                    ", host=" + host +
                    ", playerCount=" + playerCount +
                    '}';
        }
    }
}
