package edu.aau.se2.server.networking.dto;

import java.util.List;

import edu.aau.se2.server.data.Player;

public class PlayersChangedMessage extends InLobbyMessage {
    private List<Player> players;

    public PlayersChangedMessage() {
        super();
    }

    public PlayersChangedMessage(int lobbyID, int fromPlayerID, List<Player> players) {
        super(lobbyID, fromPlayerID);
        this.players = players;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }
}
