package edu.aau.se2.server.networking.dto;

import java.util.ArrayList;

import edu.aau.se2.server.data.Player;

public class PlayersChangedMessage extends InLobbyMessage {
    private ArrayList<Player> players;

    public PlayersChangedMessage() {
        super();
    }

    public PlayersChangedMessage(int lobbyID, int fromPlayerID, ArrayList<Player> players) {
        super(lobbyID, fromPlayerID);
        this.players = players;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }
}
