package edu.aau.se2.server.networking.dto;

import java.util.ArrayList;
import edu.aau.se2.server.data.Player;

public class StartGameMessage extends InLobbyMessage {
    private ArrayList<Player> players;
    private int startArmyCount;

    public StartGameMessage() {
        super();
    }

    public StartGameMessage(int lobbyID, int fromPlayerID, ArrayList<Player> players, int startArmyCount) {
        super(lobbyID, fromPlayerID);
        this.players = players;
        this.startArmyCount = startArmyCount;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public int getStartArmyCount() {
        return startArmyCount;
    }

    public void setStartArmyCount(int startArmyCount) {
        this.startArmyCount = startArmyCount;
    }
}