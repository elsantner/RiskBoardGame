package edu.aau.se2;


import com.badlogic.gdx.Game;


public class RiskGame extends Game {

    @Override
    public void create() {

        LobbyScreen lobbyScreen = new LobbyScreen(createLobby());
        setScreen(lobbyScreen);

    }

    public Lobby createLobby() {
        Lobby lobby = new Lobby();
        lobby.createLobby();
        return lobby;
    }
}
