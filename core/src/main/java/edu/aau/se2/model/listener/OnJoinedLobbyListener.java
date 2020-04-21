package edu.aau.se2.model.listener;

import java.util.List;

import edu.aau.se2.server.data.Player;

public interface OnJoinedLobbyListener {
    void joinedLobby(int lobbyID, Player host, List<Player> players);
}
