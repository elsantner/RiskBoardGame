package edu.aau.se2.model.listener;

import java.util.List;

import edu.aau.se2.server.networking.dto.LobbyListMessage;

public interface OnLobbyListChangedListener {
    void lobbyListChanged(List<LobbyListMessage.LobbyData> lobbyList);
}
