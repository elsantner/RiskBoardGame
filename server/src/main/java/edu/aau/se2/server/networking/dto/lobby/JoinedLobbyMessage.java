package edu.aau.se2.server.networking.dto.lobby;

import java.util.List;

import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.InLobbyMessage;

public class JoinedLobbyMessage extends InLobbyMessage {
    private Player host;
    private List<Player> players;

    public JoinedLobbyMessage() {
    }

    public JoinedLobbyMessage(int lobbyID, int fromPlayerID, List<Player> players, Player host) {
        super(lobbyID, fromPlayerID);
        this.players = players;
        this.host = host;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Player getHost() {
        return host;
    }

    public void setHost(Player host) {
        this.host = host;
    }
}
