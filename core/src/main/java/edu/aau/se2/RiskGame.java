package edu.aau.se2;


import com.badlogic.gdx.Game;

import edu.aau.se2.view.lobbylist.LobbyListScreen;


public class RiskGame extends Game {

    @Override
    public void create() {

        //LobbyScreen lobbyScreen = new LobbyScreen(createLobby());
        LobbyListScreen lobbyListScreen = new LobbyListScreen(this);
        setScreen(lobbyListScreen);

    }

    public Lobby createLobby() {
        Lobby lobby = new Lobby();
        lobby.createLobby();
        return lobby;
    }
}
