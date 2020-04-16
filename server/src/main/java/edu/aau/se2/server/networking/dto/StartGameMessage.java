package edu.aau.se2.server.networking.dto;

import java.util.List;

import edu.aau.se2.server.data.Player;

public class StartGameMessage extends InLobbyMessage {
    private List<Player> players;
    private int startArmyCount;

    public StartGameMessage() {
        super();
    }

    public StartGameMessage(int lobbyID, int fromPlayerID, List<Player> players, int startArmyCount) {
        super(lobbyID, fromPlayerID);
        this.players = players;
        this.startArmyCount = startArmyCount;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public int getStartArmyCount() {
        return startArmyCount;
    }

    public void setStartArmyCount(int startArmyCount) {
        this.startArmyCount = startArmyCount;
    }
}